/**
 * 
 */
package adn.factory.generic;

import org.springframework.stereotype.Component;

import adn.factory.EMFactory;
import adn.factory.EMProductionException;
import adn.factory.Factory;
import adn.model.Entity;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
@EMFactory
public class EntityFactory<E extends Entity, M extends Model> implements Factory<E, M> {

	@Override
	public E produceEntity(M model, Class<E> clazz) throws EMProductionException {
		// TODO Auto-generated method stub
		E entity = Factory.super.produceEntity(model, clazz);

		entity.setId(model.getId());

		return serviceManager.getService(clazz).executeDefaultProcedure(entity);
	}

}
