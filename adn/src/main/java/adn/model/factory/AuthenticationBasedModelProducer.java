/**
 * 
 */
package adn.model.factory;

import java.util.List;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelProducer<T, P> extends ModelProducer<T, P> {

	P produce(T source, Role role);

	P produceImmutable(T source, Role role);

	List<P> produce(List<T> source, Role role);

	List<P> produceImmutable(List<T> source, Role role);

}
