package adn.model.factory;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;
import adn.model.models.Model;

@Component(DelegateEntityExtractorProvider.NAME)
@Order(value = 4)
public class DelegateEntityExtractorProvider implements EntityExtractorProvider {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "defaultEntityExtractorProvider";
	private static final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.extraction";

	private Map<Class<? extends Entity>, EntityExtractor<? extends Entity, ? extends Model>> extractorMap;
	private static final EntityExtractor<?, ?> DEFAULT_EXTRACTOR = new EntityExtractor<Entity, Model>() {};

	@Autowired
	private ModelContextProvider modelManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());
		this.extractorMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EntityExtractor.class));
		scanner.addIncludeFilter(new AnnotationTypeFilter(Generic.class));
		scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(ENTITY_EXTRACTOR_PACKAGE)) {
				Class<? extends EntityExtractor> clazz = (Class<? extends EntityExtractor>) Class
						.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				if (!Entity.class.isAssignableFrom(anno.entityGene())) {
					continue;
				}

				this.extractorMap.put((Class<? extends Entity>) anno.entityGene(),
						(EntityExtractor<?, ?>) ContextProvider.getApplicationContext()
								.getBean(StringHelper.toCamel(clazz.getSimpleName(), null)));
			}
		} catch (Exception any) {
			any.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		modelManager.getEntityTree().forEach(node -> {
			if (extractorMap.get(node.getNode()) == null) {
				if (node.getParent() == null) {
					extractorMap.put((Class<? extends Entity>) node.getNode(), DEFAULT_EXTRACTOR);
					return;
				}

				EntityExtractor<?, ?> parentExtractor = extractorMap.get(node.getParent().getNode());

				extractorMap.put((Class<? extends Entity>) node.getNode(),
						parentExtractor != null ? parentExtractor : DEFAULT_EXTRACTOR);
			}
		});
		extractorMap.forEach((k, v) -> logger.info(String.format("Register one %s of type [%s] for [%s]",
				EntityExtractor.class.getName(), v.getClass().getName(), k.getName())));
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity, M extends Model> EntityExtractor<T, M> getExtractor(Class<T> entityClass) {
		return (EntityExtractor<T, M>) this.extractorMap.get(entityClass);
	}

}
