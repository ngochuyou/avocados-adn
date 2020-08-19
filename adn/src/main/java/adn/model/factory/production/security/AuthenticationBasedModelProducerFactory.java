package adn.model.factory.production.security;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.application.managers.ModelManager;
import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.models.Model;

@Component
@Order(value = 5)
public class AuthenticationBasedModelProducerFactory implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Model>, AuthenticationBasedModelProducer<?, ?>> producerMap;

	private final String MODEL_PRODUCER_PACKAGE = "adn.model.factory.production.security";

	private AuthenticationBasedModelProducer<Entity, Model> defaultProducer = new AuthenticationBasedModelProducer<Entity, Model>() {};

	@Autowired
	private ModelManager modelManager;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		logger.info("Initializing Model Producers");
		this.producerMap = new HashMap<>();
		// @formatter:off
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Genetized.class));
		scanner.findCandidateComponents(MODEL_PRODUCER_PACKAGE).forEach(bean -> {
			try {
				Class<? extends AuthenticationBasedModelProducer<?, ?>> clazz = (Class<? extends AuthenticationBasedModelProducer<?, ?>>) Class
						.forName(bean.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				this.producerMap.put(anno.modelGene(), context.getBean(clazz));
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(context);
			}
		});
		modelManager.getRelationMap().values().forEach(set -> set.forEach(clazz -> {
			if (this.producerMap.get(clazz) == null) {
				this.producerMap.put(clazz, defaultProducer);
			}
		}));
		modelManager.getModelTree().forEach(node -> {
			if (node.getParent() == null) {
				return;
			}

			AuthenticationBasedModelProducer<?, ?> comp = null;
			AuthenticationBasedModelProducer<?, ?> parent = this.producerMap.get(node.getParent().getNode());
			AuthenticationBasedModelProducer<?, ?> child = this.producerMap.get(node.getNode());

			if (parent != null && child != null) {
				if (!parent.equals(defaultProducer) && !child.equals(defaultProducer)) {
					comp = parent.and(child);
				} else {
					comp = !parent.equals(defaultProducer) ? parent : child;
				}
			} else {
				comp = parent != null ? parent : child;
			}

			this.producerMap.put(node.getNode(), comp == null ? defaultProducer : comp);
		});
		this.producerMap.forEach((k, v) -> {
			logger.info(v.getName() + " has been built for production of " + k.getName());
		});
		// @formatter:on
		logger.info("Finished initializing Model Producers");
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity, M extends Model> AuthenticationBasedModelProducer<T, M> getProducer(Class<M> modelClass) {

		return (AuthenticationBasedModelProducer<T, M>) this.producerMap.get(modelClass);
	}

}
