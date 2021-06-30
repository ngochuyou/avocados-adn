package adn.model.factory;

import java.util.Collections;
import java.util.Map;

import adn.model.AbstractModel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface AuthenticationBasedModelProducer<T extends AbstractModel> extends ModelProducer<T> {

	@Override
	default Map<String, Object> produce(T entity) {
		return produce(entity, null);
	}

	@Override
	default Map<String, Object> produceImmutable(T entity) {
		return Collections.unmodifiableMap(produce(entity, null));
	}

	Map<String, Object> produce(T entity, Role role);

	Map<String, Object> produceImmutable(T entity, Role role);

}
