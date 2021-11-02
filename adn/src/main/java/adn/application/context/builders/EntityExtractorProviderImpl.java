package adn.application.context.builders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.factory.extraction.PojoEntityExtractor;
import adn.model.factory.extraction.PojoEntityExtractorContract;
import adn.model.factory.extraction.PojoEntityExtractorProvider;
import adn.model.factory.extraction.Synthesized;
import adn.model.factory.extraction.SyntheticPojoEntityExtractor;
import adn.model.models.Model;

@Component(EntityExtractorProviderImpl.NAME)
@Primary
public class EntityExtractorProviderImpl implements PojoEntityExtractorProvider, ContextBuilder {

	public static final String NAME = "entityExtractorProviderImpl";
	private static final String ENTITY_EXTRACTOR_PACKAGE = "adn.model.factory.extraction";

	private Map<Class<? extends DomainEntity>, PojoEntityExtractor<? extends DomainEntity, ? extends DomainEntity>> extractorMap;
	private static final PojoEntityExtractor<?, ?> DEFAULT_EXTRACTOR = new PojoEntityExtractorContract<DomainEntity, DomainEntity>() {
		@Override
		public <E extends DomainEntity, N extends DomainEntity> E extract(N source, E target) {
			return target;
		}

		@Override
		public String getLoggableName() {
			return "DEFAULT_EXTRACTOR";
		}
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass().getName());
		Map<Class<? extends DomainEntity>, PojoEntityExtractor<? extends DomainEntity, ? extends DomainEntity>> extractorMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(PojoEntityExtractor.class));

		ModelContextProvider modelManager = ContextProvider.getBean(ModelContextProvider.class);

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(ENTITY_EXTRACTOR_PACKAGE)) {
				Class<? extends PojoEntityExtractor> type = (Class<? extends PojoEntityExtractor>) Class
						.forName(beanDef.getBeanClassName());

				if (SyntheticPojoEntityExtractor.class.isAssignableFrom(type)) {
					continue;
				}

				Generic anno = type.getDeclaredAnnotation(Generic.class);

				if (!Entity.class.isAssignableFrom(anno.entityGene())) {
					continue;
				}

				extractorMap.put(anno.entityGene(), (PojoEntityExtractor<?, ?>) ContextProvider.getApplicationContext()
						.getBean(TypeHelper.getComponentName(type)));
			}
		} catch (Exception any) {
			any.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		modelManager.getModelTree().forEach(tree -> {
			Class<? extends Model> modelType = (Class<? extends Model>) tree.getNode();
			Synthesized synthesizedAnno = modelType.getDeclaredAnnotation(Synthesized.class);

			if (synthesizedAnno == null) {
				return;
			}

			Generic genericAnno = modelType.getDeclaredAnnotation(Generic.class);

			if (genericAnno == null) {
				logger.warn(String.format("%s annotation not found on %s", Generic.class, Synthesized.class,
						Model.class.getName()));
				return;
			}

			extractorMap.put(genericAnno.entityGene(),
					new SyntheticPojoEntityExtractor(genericAnno.entityGene(), modelType));
		});

		modelManager.getEntityTree().forEach(tree -> {
			Class<? extends DomainEntity> type = tree.getNode();
			PojoEntityExtractor parentExtractor = Optional.ofNullable(tree.getParent())
					.map(parentTree -> extractorMap.get(parentTree.getNode())).orElse(null);
			PojoEntityExtractor currentExtractor = extractorMap.get(type);

			if (currentExtractor == null) {
				extractorMap.put(type, Optional.ofNullable(parentExtractor).orElse(DEFAULT_EXTRACTOR));
				return;
			}

			if (parentExtractor != null && !parentExtractor.equals(DEFAULT_EXTRACTOR)) {
				extractorMap.put(type, parentExtractor.and(currentExtractor));
			}
		});
		extractorMap.forEach((k, v) -> logger.debug(String.format("[%s] -> [%s]", k.getName(), v.getLoggableName())));
		this.extractorMap = Collections.unmodifiableMap(extractorMap);
		logger.info("Finished building " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	public <T extends DomainEntity, M extends DomainEntity> PojoEntityExtractor<T, M> getExtractor(
			Class<T> entityClass) {
		return (PojoEntityExtractor<T, M>) extractorMap.get(entityClass);
	}

}
