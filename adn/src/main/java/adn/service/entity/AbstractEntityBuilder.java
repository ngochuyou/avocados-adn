/**
 * 
 */
package adn.service.entity;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.dao.EntityBuilder;
import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Entity.class)
public class AbstractEntityBuilder<T extends Entity> implements EntityBuilder<T> {

	@SuppressWarnings("unchecked")
	private T loadPersistence(final T model) {
		return (T) ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession()
				.load(model.getClass(), model.getId());
	}

	@Override
	public T defaultBuild(final T model) {
		return loadPersistence(model);
	}

	@Override
	public T insertionBuild(final T model) {
		return loadPersistence(model);
	}

	@Override
	public T updateBuild(final T model) {
		return loadPersistence(model);
	}

	@Override
	public T deactivationBuild(final T model) {
		T persistence = loadPersistence(model);

		persistence.setDeactivatedDate(new Date());

		return persistence;
	}

}
