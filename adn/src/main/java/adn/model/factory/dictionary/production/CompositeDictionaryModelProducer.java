/**
 * 
 */
package adn.model.factory.dictionary.production;

import java.util.Map;

import adn.model.AbstractModel;
import adn.model.factory.CompositeModelProducer;

/**
 * @author Ngoc Huy
 *
 */
public interface CompositeDictionaryModelProducer<T extends AbstractModel>
		extends DictionaryModelProducer<T>, CompositeModelProducer<T, Map<String, Object>> {

	<E extends T> CompositeDictionaryModelProducer<E> and(CompositeDictionaryModelProducer<E> next);

	@Override
	default <E extends T> CompositeDictionaryModelProducer<E> and(CompositeModelProducer<E, Map<String, Object>> next) {
		return and((CompositeDictionaryModelProducer<E>) next);
	}

}
