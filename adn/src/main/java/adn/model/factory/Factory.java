package adn.model.factory;

import adn.model.Entity;
import adn.model.Model;

public interface Factory {

	<E extends Entity, M extends Model> E produce(M model, Class<E> clazz);

	<E extends Entity, M extends Model> M produce(E entity, Class<M> clazz);

}
