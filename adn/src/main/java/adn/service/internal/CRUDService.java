/**
 * 
 */
package adn.service.internal;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import adn.application.context.ContextProvider;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface CRUDService<T extends Entity> extends Service {

	<E extends T> DatabaseInteractionResult<E> create(E model, Class<E> type);

	<E extends T> DatabaseInteractionResult<E> update(E model, Class<E> type);

	<E extends T> DatabaseInteractionResult<E> remove(E model, Class<E> type);

	default Session getCurrentSession() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();
	}

}
