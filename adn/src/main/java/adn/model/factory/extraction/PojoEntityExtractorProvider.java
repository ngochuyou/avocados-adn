package adn.model.factory.extraction;

import adn.application.context.internal.ContextBuilder;
import adn.model.DomainEntity;

public interface PojoEntityExtractorProvider extends ContextBuilder {

	<T extends DomainEntity, M extends DomainEntity> PojoEntityExtractor<T, M> getExtractor(Class<T> entityClass);

}
