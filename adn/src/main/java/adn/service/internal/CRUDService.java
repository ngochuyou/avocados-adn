/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.data.domain.Pageable;

import adn.application.context.ContextProvider;
import adn.dao.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface CRUDService extends Service {

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns)
			throws NoSuchFieldException;

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns, Role role)
			throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable)
			throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable,
			Role role) throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, Serializable associationIdentifier,
			Collection<String> columns, Pageable pageable, Role role) throws NoSuchFieldException;

	default <T extends Entity, E extends T> DatabaseInteractionResult<E> create(Serializable id, E model,
			Class<E> type) {
		return create(id, model, type, false);
	}

	<T extends Entity, E extends T> DatabaseInteractionResult<E> create(Serializable id, E model, Class<E> type,
			boolean flushOnFinish);

	default <T extends Entity, E extends T> DatabaseInteractionResult<E> update(Serializable id, E model,
			Class<E> type) {
		return update(id, model, type, false);
	}

	<T extends Entity, E extends T> DatabaseInteractionResult<E> update(Serializable id, E model, Class<E> type,
			boolean flushOnFinish);

	default Session getCurrentSession() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();
	}
}
