package adn.model.factory;

import adn.model.entities.Entity;
import adn.model.models.Model;

public interface ModelProducer<T extends Entity, M extends Model> {

	M produce(T entity, M model);

	default String getName() {

		return this.getClass().getName();
	}

}
