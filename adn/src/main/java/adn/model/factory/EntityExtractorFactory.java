package adn.model.factory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.application.managers.ModelManager;
import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.models.Model;

@Component
@Order(value = 4)
public class EntityExtractorFactory implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.extraction";

	private Map<Class<? extends Entity>, EntityExtractor<? extends Entity, ? extends Model>> extractorMap;

	private EntityExtractor<?, ?> defaultExtractor = new EntityExtractor<Entity, Model>() {};

	@Autowired
	private ModelManager modelManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
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
	public <T extends Entity, M extends Model> EntityExtractor<T, M> getExtractor(Class<T> entityClass) {

		return (EntityExtractor<T, M>) this.extractorMap.get(entityClass);
	}

}
