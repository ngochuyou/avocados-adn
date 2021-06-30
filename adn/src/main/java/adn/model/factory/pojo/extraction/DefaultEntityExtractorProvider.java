package adn.model.factory.pojo.extraction;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.AbstractModel;
import adn.model.Generic;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;

@Component(DefaultEntityExtractorProvider.NAME)
@Order(value = 4)
public class DefaultEntityExtractorProvider implements EntityExtractorProvider {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "defaultEntityExtractorProvider";
	private static final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.pojo.extraction";

	private Map<Class<? extends AbstractModel>, PojoEntityExtractor<? extends AbstractModel, ? extends AbstractModel>> extractorMap;
	private static final PojoEntityExtractor<?, ?> DEFAULT_EXTRACTOR = new PojoEntityExtractor<AbstractModel, AbstractModel>() {
		@Override
		public AbstractModel extract(AbstractModel model) {
			return new AbstractModel() {};
		}

		@Override
		public AbstractModel extract(AbstractModel source, AbstractModel target) {
			return target;
		}
	};

	@Autowired
	private ModelContextProvider modelManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());
		this.extractorMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(PojoEntityExtractor.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(ENTITY_EXTRACTOR_PACKAGE)) {
				Class<? extends PojoEntityExtractor> clazz = (Class<? extends PojoEntityExtractor>) Class
						.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				if (!Entity.class.isAssignableFrom(anno.entityGene())) {
					continue;
				}

				this.extractorMap.put(anno.entityGene(), (PojoEntityExtractor<?, ?>) ContextProvider
						.getApplicationContext().getBean(TypeHelper.getComponentName(clazz)));
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

				PojoEntityExtractor<?, ?> parentExtractor = extractorMap.get(node.getParent().getNode());

				extractorMap.put((Class<? extends Entity>) node.getNode(),
						parentExtractor != null ? parentExtractor : DEFAULT_EXTRACTOR);
			}
		});
		extractorMap.forEach((k, v) -> logger.info(String.format("Register one %s of type [%s] for [%s]",
				PojoEntityExtractor.class.getName(), v.getClass().getName(), k.getName())));
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractModel, M extends AbstractModel> PojoEntityExtractor<T, M> getExtractor(
			Class<T> entityClass) {
		return (PojoEntityExtractor<T, M>) this.extractorMap.get(entityClass);
	}

}
