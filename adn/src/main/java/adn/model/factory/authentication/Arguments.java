/**
 * 
 */
package adn.model.factory.authentication;

/**
 * @author Ngoc Huy
 *
 */
public interface Arguments<T> {

	T getSource();

	<X extends Arguments<T>, E extends X> E unwrap(Class<E> type);
	
}
