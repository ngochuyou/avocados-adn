/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.application.context.ContextProvider;
import adn.dao.generic.Result;
import adn.dao.generic.ResultBatch;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface CRUDService extends Service {

	default <T extends Entity, E extends T> Result<E> create(Serializable id, E model, Class<E> type) {
		return create(id, model, type, false);
	}

	<T extends Entity, E extends T> Result<E> create(Serializable id, E model, Class<E> type, boolean flushOnFinish);

	default <T extends Entity, E extends T> ResultBatch<E> createBatch(Collection<E> batch, Class<E> type) {
		return createBatch(batch, type, false);
	};

	<T extends Entity, E extends T> ResultBatch<E> createBatch(Collection<E> batch, Class<E> type,
			boolean flushOnFinish);

	default <T extends Entity, E extends T> Result<E> update(Serializable id, E model, Class<E> type) {
		return update(id, model, type, false);
	}

	<T extends Entity, E extends T> Result<E> update(Serializable id, E model, Class<E> type, boolean flushOnFinish);

	default Session getCurrentSession() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();
	}

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns)
			throws NoSuchFieldException;

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns, Role role)
			throws NoSuchFieldException;

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			UUID departmentId) throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable)
			throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable,
			Role role) throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable,
			UUID departmentId) throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Role role)
			throws NoSuchFieldException;

	<T extends Entity> List<String> getDefaultColumns(Class<T> type, Role role, Collection<String> columns)
			throws NoSuchFieldException;

	<T extends Entity> List<String> getDefaultColumns(Class<T> type, UUID departmentId, Collection<String> columns)
			throws NoSuchFieldException;

	<T extends Entity, E extends T> Map<String, Object> find(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Pageable pageable, Role role) throws NoSuchFieldException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Sort sort, Role role) throws NoSuchFieldException;

	<T extends Entity, E extends T> Map<String, Object> find(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Pageable pageable, UUID departmentId) throws NoSuchFieldException;

	<T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Sort sort, UUID departmentId) throws NoSuchFieldException;

}
