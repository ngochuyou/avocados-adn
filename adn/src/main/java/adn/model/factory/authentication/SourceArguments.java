/**
 * 
 */
package adn.model.factory.authentication;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface SourceArguments<E extends DomainEntity, T> extends Arguments<T> {

	SourceMetadata<E> getMetadata();

}
