/**
 * 
 */
package adn.model.factory.authentication;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<T extends DomainEntity, S> {

	T extract(S source);

	T extract(S source, T target);

}
