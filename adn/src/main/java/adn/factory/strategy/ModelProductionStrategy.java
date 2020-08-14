package adn.factory.strategy;

import java.lang.reflect.InvocationTargetException;

import adn.model.Entity;
import adn.model.Model;

public interface ModelProductionStrategy<M extends Model, E extends Entity> {

	M produce(E entity, Class<M> targetModelClass) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

}
