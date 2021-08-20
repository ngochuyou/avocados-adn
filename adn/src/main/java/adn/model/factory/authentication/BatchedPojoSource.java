/**
 * 
 */
package adn.model.factory.authentication;

import java.util.List;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface BatchedPojoSource<T extends DomainEntity> extends PojoSource<T, List<T>> {

	@Override
	List<T> getSource();

}
