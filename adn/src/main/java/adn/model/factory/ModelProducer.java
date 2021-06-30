package adn.model.factory;

import java.util.Map;

import adn.model.AbstractModel;

public interface ModelProducer<T extends AbstractModel> {

	Map<String, Object> produce(T entity);

	Map<String, Object> produceImmutable(T entity);
	
}
