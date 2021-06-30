/**
 * 
 */
package adn.model.factory;

import java.util.Map;

import adn.model.AbstractModel;

/**
 * @author Ngoc Huy
 *
 */
public interface CompositeModelProducer<T extends AbstractModel> extends ModelProducer<T> {

	Map<String, Object> produce(T entity, Map<String, Object> modelMap);

	Map<String, Object> produceImmutable(T entity, Map<String, Object> modelMap);

	<E extends T> CompositeModelProducer<E> and(CompositeModelProducer<E> next);

}
