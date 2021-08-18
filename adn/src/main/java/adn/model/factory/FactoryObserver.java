/**
 * 
 */
package adn.model.factory;

import java.util.Map;

/**
 * @author Ngoc Huy
 *
 */
public interface FactoryObserver<T, S, P, F extends ModelProducer<S, P>> {

	void afterFactoryBuild(Map<Class<? extends T>, F> producers);

}
