/**
 * 
 */
package adn.factory;

import java.lang.reflect.InvocationTargetException;

import adn.application.ApplicationContextProvider;
import adn.application.managers.ServiceProvider;
import adn.model.Entity;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface Factory<E extends Entity, M extends Model> {

	final ServiceProvider serviceManager = ApplicationContextProvider.getApplicationContext()
			.getBean(ServiceProvider.class);

	default E produceEntity(M model, Class<E> clazz) throws EMProductionException {
		try {
			E entity = clazz.getConstructor().newInstance();

			entity.setActive(model.isActive());
			entity.setCreatedDate(model.getCreatedDate());
			entity.setUpdatedDate(model.getUpdatedDate());
			entity.setDeactivatedDate(model.getDeactivatedDate());

			return entity;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new EMProductionException(this.getClass());
		}
	}

	default M produceModel(E entity, Class<M> clazz) {
		try {
			M model = clazz.getConstructor().newInstance();

			model.setActive(entity.isActive());
			model.setCreatedDate(entity.getCreatedDate());
			model.setUpdatedDate(entity.getUpdatedDate());
			model.setDeactivatedDate(entity.getDeactivatedDate());

			return model;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			throw new EMProductionException(this.getClass());
		}
	}

}
