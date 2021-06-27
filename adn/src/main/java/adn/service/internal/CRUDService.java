/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import adn.application.context.ContextProvider;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface CRUDService extends Service {

	<T extends Entity, E extends T> DatabaseInteractionResult<E> create(Serializable id, E model, Class<E> type);

	<T extends Entity, E extends T> DatabaseInteractionResult<E> create(E model, Class<E> type);

	<T extends Entity, E extends T> DatabaseInteractionResult<E> update(Serializable id, E model, Class<E> type);

	<T extends Entity, E extends T> DatabaseInteractionResult<E> update(E model, Class<E> type);

	<T extends Entity, E extends T> DatabaseInteractionResult<E> remove(Serializable id, E model, Class<E> type);

	<T extends Entity, E extends T> DatabaseInteractionResult<E> remove(E model, Class<E> type);

	default Session getCurrentSession() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();
	}

}
