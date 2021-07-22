package adn.model.factory.dictionary.production;

import java.util.Map;

import adn.model.DomainEntity;
import adn.model.factory.ModelProducer;

public interface DictionaryModelProducer<T extends DomainEntity> extends ModelProducer<T, Map<String, Object>> {

}
