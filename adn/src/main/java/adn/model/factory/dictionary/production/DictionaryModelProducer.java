package adn.model.factory.dictionary.production;

import java.util.Map;

import adn.model.AbstractModel;
import adn.model.factory.ModelProducer;

public interface DictionaryModelProducer<T extends AbstractModel> extends ModelProducer<T, Map<String, Object>> {

}
