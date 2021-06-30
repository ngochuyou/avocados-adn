package adn.model.factory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.model.AbstractModel;
import adn.model.Generic;
import adn.model.ModelContextProvider;
import adn.model.ModelInheritanceTree;
import adn.model.factory.production.security.DefaultModelProducer;
import adn.service.internal.Role;

@Component(DefaultAuthenticationBasedModelFactory.NAME)
@Order(value = 5)
public class DefaultAuthenticationBasedModelFactory implements AuthenticationBasedModelFactory {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "authenticationBasedProducerProvider";
	private static final String MODEL_PRODUCER_PACKAGE = "adn.model.factory.production.security";

	private Map<Class<? extends AbstractModel>, CompositeAuthenticationBasedModelProducer<? extends AbstractModel>> producerMap;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass());
		this.producerMap = new HashMap<>();
		// @formatter:off
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(CompositeAuthenticationBasedModelProducer.class));

		ModelContextProvider modelsDescriptor = ContextProvider.getApplicationContext().getBean(ModelContextProvider.class);
		
		try {
			for (BeanDefinition beanDef: scanner.findCandidateComponents(MODEL_PRODUCER_PACKAGE)) {
				Class<? extends CompositeAuthenticationBasedModelProducer<?>> clazz = (Class<? extends CompositeAuthenticationBasedModelProducer<?>>) Class
						.forName(beanDef.getBeanClassName());
				
				if (clazz.equals(DefaultModelProducer.class)) {
					continue;
				}
				
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				this.producerMap.put(anno.entityGene(), ContextProvider.getApplicationContext().getBean(clazz));
			}
			
			modelsDescriptor.getEntityTree().forEach(branch -> {
				if (DefaultModelProducer.shouldUse(branch.getNode())) {
					producerMap.computeIfAbsent(branch.getNode(), DefaultModelProducer::new);
				}
			});
			modelsDescriptor.getEntityTree().forEach(branch -> {
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
				logger.info(String.format("Registered one %s for type [%s]: %s", CompositeAuthenticationBasedModelProducer.class.getSimpleName(), k.getName(), v.getName()));
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
		// @formatter:on
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	private <C extends AbstractModel> CompositeAuthenticationBasedModelProducer<C> from(
			CompositeAuthenticationBasedModelProducer<?> target) {
		return (CompositeAuthenticationBasedModelProducer<C>) target;
	}

	private <T extends AbstractModel, E extends T> CompositeAuthenticationBasedModelProducer<E> combine(
			CompositeAuthenticationBasedModelProducer<T> parent, CompositeAuthenticationBasedModelProducer<E> child) {
		if (parent == null || child == null) {
			throw new IllegalStateException(String.format("Internal error: one node in the %s was null",
					CompositeAuthenticationBasedModelProducer.class.getName()));
		}

		return parent.and(child);
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractModel> CompositeAuthenticationBasedModelProducer<T> getProducer(Class<T> type) {
		return (CompositeAuthenticationBasedModelProducer<T>) producerMap.get(type);
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity) {
		return produce(type, entity, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity, Role role) {
		return getProducer(type).produceImmutable(entity, role);
	}

}
