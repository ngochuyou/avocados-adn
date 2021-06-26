package adn.model.factory;

import adn.model.AbstractModel;
import adn.model.entities.Entity;

public interface ModelProducer<T extends Entity, M extends AbstractModel> {

	M produceForAnonymous(T entity, M model);

	default String getName() {
		return this.getClass().getName();
	}

}
