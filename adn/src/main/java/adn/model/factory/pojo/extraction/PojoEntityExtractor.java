/**
 * 
 */
package adn.model.factory.pojo.extraction;

import adn.model.DomainEntity;
import adn.model.factory.EntityExtractor;

/**
 * @author Ngoc Huy
 *
 */
public interface PojoEntityExtractor<T extends DomainEntity, M extends DomainEntity> extends EntityExtractor<T, M> {

	@Override
	T extract(M model);

	@Override
	T extract(M source, T target);

}
