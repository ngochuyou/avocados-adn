/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import adn.model.AbstractModel;
import adn.model.factory.dictionary.production.CompositeModelProducer;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface CompositeAuthenticationBasedModelProducer<T extends AbstractModel>
		extends AuthenticationBasedModelProducer<T>, CompositeModelProducer<T> {

	@Override
	default Map<String, Object> produce(T entity, Map<String, Object> modelMap) {
		return produce(entity, modelMap, null);
	}

	@Override
	default Map<String, Object> produceImmutable(T entity, Map<String, Object> modelMap) {
		return produceImmutable(entity, modelMap, null);
	}

	Map<String, Object> produce(T entity, Map<String, Object> modelMap, Role role);

	Map<String, Object> produceImmutable(T entity, Map<String, Object> modelMap, Role role);

	@Override
	default <E extends T> CompositeModelProducer<E> and(CompositeModelProducer<E> next) {
		return and((CompositeAuthenticationBasedModelProducer<E>) next);
	}

	<E extends T> CompositeAuthenticationBasedModelProducer<E> and(CompositeAuthenticationBasedModelProducer<E> next);

	default String getName() {
		return this.getClass().getSimpleName();
	}

}
