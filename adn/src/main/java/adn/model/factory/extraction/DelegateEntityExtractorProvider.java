package adn.model.factory.extraction;

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

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.model.Genetized;
import adn.model.ModelManager;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractor;
import adn.model.factory.EntityExtractorProvider;
import adn.model.models.Model;
import adn.utilities.Strings;

@Component(Constants.defaultEntityExtractorProdiverName)
@Order(value = 4)
public class DelegateEntityExtractorProvider implements EntityExtractorProvider {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.extraction";

	private Map<Class<? extends Entity>, EntityExtractor<? extends Entity, ? extends Model>> extractorMap;

	private EntityExtractor<?, ?> defaultExtractor = new EntityExtractor<Entity, Model>() {};

	@Autowired
	private ModelManager modelManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());
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

				Component componentAnno = clazz.getDeclaredAnnotation(Component.class);

				if (componentAnno == null) {
					throw new Exception(Component.class.getName() + " is not found on " + clazz.getName());
				}

				this.extractorMap.put(anno.entityGene(), (EntityExtractor<?, ?>) ContextProvider.getApplicationContext()
						.getBean(Strings.toCamel(clazz.getSimpleName(), null)));
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		});
		modelManager.getEntityTree().forEach(node -> {
			if (this.extractorMap.get(node.getNode()) == null) {
				EntityExtractor<?, ?> parentExtractor = this.extractorMap.get(node.getParent().getNode());

				this.extractorMap.put(node.getNode(), parentExtractor != null ? parentExtractor : defaultExtractor);
			}
		});
		this.extractorMap
				.forEach((k, v) -> logger.info("Assigning " + v.getName() + " for " + k.getName() + " extraction"));
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity, M extends Model> EntityExtractor<T, M> getExtractor(Class<T> entityClass) {
		logger.debug("Providing extractor for: " + entityClass);

		return (EntityExtractor<T, M>) this.extractorMap.get(entityClass);
	}

}
