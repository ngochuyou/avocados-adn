/**
 * 
 */
package adn.model.factory.authentication;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface SinglePojoSource<T extends DomainEntity> extends PojoSource<T, T> {

	@Override
	T getSource();

}
