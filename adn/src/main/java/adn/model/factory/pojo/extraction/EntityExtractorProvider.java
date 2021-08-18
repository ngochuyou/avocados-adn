package adn.model.factory.pojo.extraction;

import adn.application.context.internal.ContextBuilder;
import adn.model.DomainEntity;

public interface EntityExtractorProvider extends ContextBuilder {

	<T extends DomainEntity, M extends DomainEntity> PojoEntityExtractor<T, M> getExtractor(Class<T> entityClass);

}
