package adn.model.factory;

import adn.model.Entity;
import adn.model.Model;

public interface ModelProducer<M extends Model, E extends Entity> {

	M produce(E entity, M model);

}
