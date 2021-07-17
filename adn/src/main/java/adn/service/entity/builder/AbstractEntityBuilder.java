/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.helpers.EntityUtils;
import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Entity.class)
public abstract class AbstractEntityBuilder<T extends Entity> implements EntityBuilder<T> {

	@Autowired
	protected SessionFactory sessionFactory;

	protected <E> E loadPersistence(Class<E> type, Serializable id) {
		return sessionFactory.getCurrentSession().load(type, id);
	}

	@SuppressWarnings("unchecked")
	protected <P extends Entity, C extends P> C cast(P entity) {
		return (C) entity;
	}

	@Override
	public T deactivationBuild(T entity) {
		return deactivationBuild(EntityUtils.getIdentifier(entity), entity);
	}

//	@Override
//	public T insertionBuild(Serializable id, T entity) {
//		return insertionBuild(entity);
//	}
//
//	@Override
//	public T updateBuild(Serializable id, T entity) {
//		return updateBuild(entity);
//	}
//
//	@Override
//	public T deactivationBuild(Serializable id, T entity) {
//		return deactivationBuild(entity);
//	}

}
