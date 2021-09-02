/**
 * 
 */
package adn.model.factory.authentication;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<T, S> {

	T extract(S source);

	T extract(S source, T target);

}
