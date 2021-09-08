/**
 * 
 */
package adn.dao.generic;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericRepository {

	<T extends Entity> List<T> findAll(Class<T> type, Pageable paging);

	<T extends Entity> List<Object[]> findAll(Class<T> type, Collection<String> columns, Pageable paging);

	<T extends Entity> Optional<T> findById(Class<T> clazz, Serializable id);

	<T extends Entity> Optional<Object[]> findById(Class<T> clazz, Serializable id, Collection<String> columns);

	<T extends Entity> long count(Class<T> type);

	<T extends Entity> long countById(Class<T> type, Serializable id);

	<E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort);

	<E extends Entity> long count(Class<E> type, Specification<E> spec);

	<E extends Entity> Optional<Object[]> findOne(Class<E> type, Collection<String> columns, Specification<E> spec);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Pageable pageable);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec, Sort sort);

	<T extends Entity, E extends T> Result<E> insert(Class<E> type, Serializable id, E model);

	<T extends Entity, E extends T> Result<E> insert(Class<E> type, Serializable id, E model, Session session);

	<T extends Entity, E extends T> Result<E> update(Class<E> type, Serializable id, E model);

	<T extends Entity, E extends T> Result<E> update(Class<E> type, Serializable id, E model, Session session);

}
