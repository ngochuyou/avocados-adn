/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.time.LocalDateTime;

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
	@SuppressWarnings("unchecked")
	public T deactivationBuild(T entity) {
		T persistence = (T) loadPersistence(entity.getClass(), EntityUtils.getIdentifier(entity));

		persistence.setDeactivatedDate(LocalDateTime.now());

		return persistence;
	}

}
