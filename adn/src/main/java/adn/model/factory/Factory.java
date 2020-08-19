package adn.model.factory;

import adn.model.entities.Entity;
import adn.model.models.Model;

public interface Factory {

	<T extends Entity, M extends Model> T produce(M model, Class<T> clazz);

	<T extends Entity, M extends Model> M produce(T entity, Class<M> clazz);

}
