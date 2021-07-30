/**
 * 
 */
package adn.model.factory;

import java.util.List;

/**
 * @author Ngoc Huy
 *
 */
public interface CompositeModelProducer<T, P> extends ModelProducer<T, P> {

	P produce(T source, P product);

	List<P> produce(List<T> source, List<P> models);

	<E extends T> CompositeModelProducer<E, P> and(CompositeModelProducer<E, P> next);

	default String getName() {
		return this.getClass().getSimpleName();
	}

}
