package adn.model.factory.dictionary.production.authentication;

import java.util.Collections;
import java.util.Map;

import adn.model.AbstractModel;
import adn.model.factory.dictionary.production.DictionaryModelProducer;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface AuthenticationBasedModelProducer<T extends AbstractModel> extends DictionaryModelProducer<T> {

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
