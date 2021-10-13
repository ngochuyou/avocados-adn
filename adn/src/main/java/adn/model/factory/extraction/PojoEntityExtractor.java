/**
 * 
 */
package adn.model.factory.extraction;

import adn.model.DomainEntity;
import adn.model.factory.authentication.EntityExtractor;

/**
 * @author Ngoc Huy
 *
 */
public interface PojoEntityExtractor<T extends DomainEntity, M extends DomainEntity> extends EntityExtractor<T, M> {

	@Override
	<E extends T, N extends M> E extract(N model);

	@Override
	<E extends T, N extends M> E extract(N source, E target);

	<E extends T, N extends M> PojoEntityExtractor<E, N> and(PojoEntityExtractor<E, N> next);
	
}
