package adn.model.factory;

import adn.model.entities.Entity;
import adn.model.models.Model;

public interface ModelProducer<M extends Model, E extends Entity> {

	M produce(E entity, M model);

	default String getName() {

		return this.getClass().getName();
	}

}
