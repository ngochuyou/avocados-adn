/**
 * 
 */
package adn.application.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractor;
import adn.model.factory.Factory;
import adn.model.factory.production.security.AuthenticationBasedModelProducer;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(4)
public class AuthenticationBasedEMFactory implements ApplicationManager, Factory {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.extraction";

	private final String MODEL_PRODUCER_PACKAGE = "adn.model.factory.production.security";

	private Map<Class<? extends Entity>, EntityExtractor<?, ?>> extractorMap;

	private EntityExtractor<?, ?> defaultExtractor = new EntityExtractor<Entity, Model>() {};

	private Map<Class<? extends Model>, AuthenticationBasedModelProducer<?, ?>> producerMap;

	private AuthenticationBasedModelProducer<Model, Entity> defaultProducer = new AuthenticationBasedModelProducer<Model, Entity>() {};

	@Autowired
	private ModelManager modelManager;

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());
		this.initializeEntityExtractors();
		this.initializeAuthenticationBasedModelProducers();
		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initializeEntityExtractors() {
		logger.info("Initializing Entity Extractors");
		this.extractorMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EntityExtractor.class));
		scanner.findCandidateComponents(ENTITY_EXTRACTOR_PACKAGE).forEach(bean -> {
			try {
				Class<? extends EntityExtractor> clazz = (Class<? extends EntityExtractor>) Class
						.forName(bean.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				if (anno == null) {
					throw new Exception(Genetized.class.getName() + " is not found on " + clazz.getName());
				}

				this.extractorMap.put(anno.entityGene(), clazz.getConstructor().newInstance());
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(context);
			}
		});
		modelManager.getEntityTree().forEach(node -> {
			if (node.getParent() == null) {
				return;
			}

			EntityExtractor<?, ?> compositeExtractor = null;
			EntityExtractor<?, ?> childrenExtractor = this.extractorMap.get(node.getNode());
			EntityExtractor<?, ?> parentExtractor = this.extractorMap.get(node.getParent().getNode());

			if (parentExtractor != null && childrenExtractor != null) {
				compositeExtractor = parentExtractor.and(childrenExtractor);
			} else {
				compositeExtractor = parentExtractor == null ? childrenExtractor : childrenExtractor;
			}

			this.extractorMap.put(node.getNode(), compositeExtractor == null ? defaultExtractor : compositeExtractor);
		});
		this.extractorMap
				.forEach((k, v) -> logger.info("Assigning " + v.getName() + " for " + k.getName() + " extraction"));
		logger.info("Finished initializing Entity Extractors");
	}

	@SuppressWarnings("unchecked")
	private void initializeAuthenticationBasedModelProducers() {
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

	@Override
	public <E extends Entity, M extends Model> E produce(M model, Class<E> clazz) {
		// TODO Auto-generated method stub
		try {
			return this.getEntityExtractor(clazz).extract(model, clazz.getConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public <E extends Entity, M extends Model> M produce(E entity, Class<M> clazz) {
		// TODO Auto-generated method stub
		try {
			return this.getModelProducer(clazz).produce(entity, clazz.getConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, M extends Model> EntityExtractor<T, M> getEntityExtractor(Class<T> clazz) {

		return (EntityExtractor<T, M>) this.extractorMap.get(clazz);
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, M extends Model> AuthenticationBasedModelProducer<M, T> getModelProducer(
			Class<M> clazz) {

		return (AuthenticationBasedModelProducer<M, T>) this.producerMap.get(clazz);
	}

}