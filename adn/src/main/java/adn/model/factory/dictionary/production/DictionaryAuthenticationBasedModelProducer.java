package adn.model.factory.dictionary.production;

import java.util.Map;

import adn.model.AbstractModel;
import adn.model.factory.AuthenticationBasedModelProducer;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface DictionaryAuthenticationBasedModelProducer<T extends AbstractModel>
		extends DictionaryModelProducer<T>, AuthenticationBasedModelProducer<T, Map<String, Object>> {

}
