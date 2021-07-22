/**
 * 
 */
package adn.model.factory.dictionary.production;

import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface CompositeDictionaryAuthenticationBasedModelProducer<T extends DomainEntity>
		extends DictionaryAuthenticationBasedModelProducer<T>, CompositeDictionaryModelProducer<T> {

	Map<String, Object> produce(T entity, Map<String, Object> modelMap, Role role);

	Map<String, Object> produceImmutable(T entity, Map<String, Object> modelMap, Role role);

	List<Map<String, Object>> produce(List<T> source, List<Map<String, Object>> models, Role role);

	List<Map<String, Object>> produceImmutable(List<T> source, List<Map<String, Object>> models, Role role);

	@Override
	default <E extends T> CompositeDictionaryModelProducer<E> and(CompositeDictionaryModelProducer<E> next) {
		return and((CompositeDictionaryAuthenticationBasedModelProducer<E>) next);
	}

	<E extends T> CompositeDictionaryAuthenticationBasedModelProducer<E> and(
			CompositeDictionaryAuthenticationBasedModelProducer<E> next);

}
