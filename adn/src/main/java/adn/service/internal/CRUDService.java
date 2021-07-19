/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;
import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.data.domain.Pageable;

import adn.application.context.ContextProvider;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface CRUDService extends Service {

	<T extends Entity, E extends T> Map<String, Object> find(Serializable id, Class<E> type, String[] columns)
			throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> Map<String, Object> find(Serializable id, Class<E> type, String[] columns,
			Role role) throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, String[] columns, Pageable pageable)
			throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, String[] columns, Pageable pageable,
			String[] groupByColumns) throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, String[] columns, Pageable pageable,
			String[] groupByColumns, Role role) throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Pageable pageable)
			throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Pageable pageable,
			String[] groupByColumns) throws SQLSyntaxErrorException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Pageable pageable,
			String[] groupByColumns, Role role) throws SQLSyntaxErrorException;

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
