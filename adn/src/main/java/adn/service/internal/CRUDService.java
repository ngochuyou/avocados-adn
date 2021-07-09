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
