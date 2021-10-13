/**
 * 
 */
package adn.model.factory.authentication;

import adn.application.Loggable;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<T, S> extends Loggable {

	<E extends T, N extends S> E extract(N source);

	<E extends T, N extends S> E extract(N source, E target);

}
