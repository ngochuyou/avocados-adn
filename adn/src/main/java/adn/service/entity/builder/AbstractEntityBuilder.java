/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.EntityUtils;
import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Entity.class)
public class AbstractEntityBuilder<T extends Entity> implements EntityBuilder<T> {

	protected <E> E loadPersistence(Class<E> type, Serializable id) {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession().load(type, id);
	}

	@Override
	public T deactivationBuild(T entity) {
		return deactivationBuild(EntityUtils.getIdentifier(entity), entity);
	}

	@Override
	public T insertionBuild(Serializable id, T entity) {
		return insertionBuild(entity);
	}

	@Override
	public T updateBuild(Serializable id, T entity) {
		return updateBuild(entity);
	}

	@Override
	public T deactivationBuild(Serializable id, T entity) {
		return updateBuild(entity);
	}

}
