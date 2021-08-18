package adn.model.factory.dictionary.production;

import java.util.Map;

import adn.model.DomainEntity;
import adn.model.factory.AuthenticationBasedModelProducer;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface DictionaryAuthenticationBasedModelProducer<T extends DomainEntity>
		extends DictionaryModelProducer<T>, AuthenticationBasedModelProducer<T, Map<String, Object>> {

}
