package adn.application.context.builders;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.factory.pojo.extraction.EntityExtractorProvider;
import adn.model.factory.pojo.extraction.PojoEntityExtractor;

@Component(DefaultEntityExtractorProvider.NAME)
@Primary
public class DefaultEntityExtractorProvider implements EntityExtractorProvider {

	public static final String NAME = "defaultEntityExtractorProvider";
	private static final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.pojo.extraction";

	private Map<Class<? extends DomainEntity>, PojoEntityExtractor<? extends DomainEntity, ? extends DomainEntity>> extractorMap;
	private static final PojoEntityExtractor<?, ?> DEFAULT_EXTRACTOR = new PojoEntityExtractor<DomainEntity, DomainEntity>() {
		@Override
		public DomainEntity extract(DomainEntity model) {
			return new DomainEntity() {};
		}

		@Override
		public DomainEntity extract(DomainEntity source, DomainEntity target) {
			return target;
		}
	};

	@Autowired
	private ModelContextProvider modelManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass().getName());
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
		extractorMap.forEach((k, v) -> logger.debug(String.format("Register one %s of type [%s] for [%s]",
				PojoEntityExtractor.class.getName(), v.getClass().getName(), k.getName())));
		logger.info("Finished building " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	public <T extends DomainEntity, M extends DomainEntity> PojoEntityExtractor<T, M> getExtractor(
			Class<T> entityClass) {
		return (PojoEntityExtractor<T, M>) this.extractorMap.get(entityClass);
	}

}
