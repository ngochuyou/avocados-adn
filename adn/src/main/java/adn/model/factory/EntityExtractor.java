/**
 * 
 */
package adn.model.factory;

import adn.model.AbstractModel;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<T extends AbstractModel, S> {

	T extract(S source);

	T extract(S source, T target);

}
