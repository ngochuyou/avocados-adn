package adn.application.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.model.AbstractModel;
import adn.model.Generic;
import adn.model.ModelContextProvider;
import adn.model.ModelInheritanceTree;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.dictionary.production.CompositeDictionaryAuthenticationBasedModelProducer;
import adn.model.factory.dictionary.production.authentication.DefaultModelProducer;
import adn.service.internal.Role;

@Component(DefaultAuthenticationBasedModelProducerFactory.NAME)
@Order(value = 5)
public class DefaultAuthenticationBasedModelProducerFactory implements AuthenticationBasedModelFactory, ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "authenticationBasedProducerProvider";
	private static final String MODEL_PRODUCER_PACKAGE = "adn.model.factory.dictionary.production.authentication";

	private Map<Class<? extends AbstractModel>, CompositeDictionaryAuthenticationBasedModelProducer<? extends AbstractModel>> producerMap;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass());
		this.producerMap = new HashMap<>();
		// @formatter:off
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(CompositeDictionaryAuthenticationBasedModelProducer.class));

		ModelContextProvider modelContextProvider = ContextProvider.getApplicationContext().getBean(ModelContextProvider.class);
		
		try {
			for (BeanDefinition beanDef: scanner.findCandidateComponents(MODEL_PRODUCER_PACKAGE)) {
				Class<? extends CompositeDictionaryAuthenticationBasedModelProducer<?>> clazz = (Class<? extends CompositeDictionaryAuthenticationBasedModelProducer<?>>) Class
						.forName(beanDef.getBeanClassName());
				
				if (clazz.equals(DefaultModelProducer.class)) {
					continue;
				}
				
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				this.producerMap.put(anno.entityGene(), ContextProvider.getApplicationContext().getBean(clazz));
			}
			
			modelContextProvider.getEntityTree().forEach(branch -> {
				if (DefaultModelProducer.shouldUse(branch.getNode())) {
					producerMap.putIfAbsent(branch.getNode(), new DefaultModelProducer<>(branch.getNode(), modelContextProvider.getMetadata(branch.getNode())));
				}
			});
			modelContextProvider.getEntityTree().forEach(branch -> {
				if (branch.getParent() == null) {
					return;
				}
				
				ModelInheritanceTree<? super AbstractModel> parent = (ModelInheritanceTree<? super AbstractModel>) branch.getParent();
				
				while (parent != null && getProducer(parent.getNode()) == null) {
					parent = parent.getParent();
				}
				
				if (parent == null) {
					return;
				}
				
				if (getProducer(branch.getNode()) == null) {
					producerMap.put(branch.getNode(), getProducer(parent.getNode()));
					return;
				}
				
				producerMap.put(branch.getNode(), combine(this.<AbstractModel>from(producerMap.get(parent.getNode())), producerMap.get(branch.getNode())));
			});
			producerMap.forEach((k, v) -> {
				logger.info(String.format("Registered one %s for type [%s]: %s", CompositeDictionaryAuthenticationBasedModelProducer.class.getSimpleName(), k.getName(), v.getName()));
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
		// @formatter:on
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	private <C extends AbstractModel> CompositeDictionaryAuthenticationBasedModelProducer<C> from(
			CompositeDictionaryAuthenticationBasedModelProducer<?> target) {
		return (CompositeDictionaryAuthenticationBasedModelProducer<C>) target;
	}

	private <T extends AbstractModel, E extends T> CompositeDictionaryAuthenticationBasedModelProducer<E> combine(
			CompositeDictionaryAuthenticationBasedModelProducer<T> parent,
			CompositeDictionaryAuthenticationBasedModelProducer<E> child) {
		if (parent == null || child == null) {
			throw new IllegalStateException(String.format("Internal error: one node in the %s was null",
					CompositeDictionaryAuthenticationBasedModelProducer.class.getName()));
		}

		return parent.and(child);
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractModel> CompositeDictionaryAuthenticationBasedModelProducer<T> getProducer(
			Class<T> type) {
		return (CompositeDictionaryAuthenticationBasedModelProducer<T>) producerMap.get(type);
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity) {
		return getProducer(type).produce(entity, new HashMap<>(16, 1.075f), ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity, Role role) {
		return getProducer(type).produce(entity, new HashMap<>(16, 1.075f), role);
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<T> entities) {
		return getProducer(type).produce(entities, IntStream.range(0, entities.size())
				.mapToObj(index -> new HashMap<String, Object>(16, 1.075f)).collect(Collectors.toList()),
				ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<T> entities, Role role) {
		return getProducer(type).produce(entities, IntStream.range(0, entities.size())
				.mapToObj(index -> new HashMap<String, Object>(16, 1.075f)).collect(Collectors.toList()), role);
	}

}