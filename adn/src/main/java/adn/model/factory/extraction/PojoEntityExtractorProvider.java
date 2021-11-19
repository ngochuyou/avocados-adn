package adn.model.factory.extraction;

import adn.model.DomainEntity;

public interface PojoEntityExtractorProvider {

	<T extends DomainEntity, M extends DomainEntity> PojoEntityExtractor<T, M> getExtractor(Class<T> entityClass);

}
