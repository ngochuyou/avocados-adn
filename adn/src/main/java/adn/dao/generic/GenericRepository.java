/**
 * 
 */
package adn.dao.generic;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Session;
import org.hibernate.SharedSessionContract;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.application.Result;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericRepository {

	public interface Selector<T> {

		List<Selection<?>> select(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder);

	}

	public interface UpdateQuerySetStatementBuilder<T> {

		CriteriaUpdate<?> build(Root<T> root, CriteriaUpdate<?> query, CriteriaBuilder builder);

	}

	public interface UpdateQueryWhereStatementBuilder<T> {

		Predicate build(Root<T> root, CriteriaUpdate<?> query, CriteriaBuilder builder);

	}

	<E extends Entity> long count(Class<E> type, Specification<E> spec);

	<E extends Entity> long count(Class<E> type, Specification<E> spec, SharedSessionContract session);

	<T extends Entity> long count(Class<T> type);

	<T extends Entity> long count(Class<T> type, SharedSessionContract session);

	<T extends Entity> long countById(Class<T> type, Serializable id);

	<T extends Entity> long countById(Class<T> type, Serializable id, SharedSessionContract session);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			LockModeType lockMode);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			LockModeType lockMode, SharedSessionContract session);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Pageable pageable);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Pageable pageable, LockModeType lockMode);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Pageable pageable, LockModeType lockMode, SharedSessionContract session);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Pageable pageable, SharedSessionContract session);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			SharedSessionContract session);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Sort sort);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Sort sort, LockModeType lockMode);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Sort sort, LockModeType lockMode, SharedSessionContract session);

	<E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Sort sort, SharedSessionContract session);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, LockModeType lockMode);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, LockModeType lockMode,
			SharedSessionContract session);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable, LockModeType lockMode);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable, LockModeType lockMode,
			SharedSessionContract session);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable,
			SharedSessionContract session);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, SharedSessionContract session);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort, LockModeType lockMode);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort, LockModeType lockMode,
			SharedSessionContract session);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort, SharedSessionContract session);

	<T extends Entity> List<Object[]> findAll(Class<T> type, Collection<String> columns, Pageable paging);

	<T extends Entity> List<Object[]> findAll(Class<T> type, Collection<String> columns, Pageable paging,
			LockModeType lockMode);

	<T extends Entity> List<Object[]> findAll(Class<T> type, Collection<String> columns, Pageable paging,
			LockModeType lockMode, SharedSessionContract session);

	<T extends Entity> List<Object[]> findAll(Class<T> type, Collection<String> columns, Pageable paging,
			SharedSessionContract session);

	<T extends Entity> List<T> findAll(Class<T> type, Pageable paging);

	<T extends Entity> List<T> findAll(Class<T> type, Pageable paging, LockModeType lockMode);

	<T extends Entity> List<T> findAll(Class<T> type, Pageable paging, LockModeType lockMode,
			SharedSessionContract session);

	<T extends Entity> List<T> findAll(Class<T> type, Pageable paging, SharedSessionContract session);

	<T extends Entity> Optional<T> findById(Class<T> clazz, Serializable id);

	<T extends Entity> Optional<Object[]> findById(Class<T> clazz, Serializable id, Collection<String> columns);

	<T extends Entity> Optional<Object[]> findById(Class<T> clazz, Serializable id, Collection<String> columns,
			LockModeType lockMode);

	<T extends Entity> Optional<Object[]> findById(Class<T> clazz, Serializable id, Collection<String> columns,
			LockModeType lockMode, SharedSessionContract session);

	<T extends Entity> Optional<Object[]> findById(Class<T> clazz, Serializable id, Collection<String> columns,
			SharedSessionContract session);

	<T extends Entity> Optional<T> findById(Class<T> clazz, Serializable id, LockModeType lockMode);

	<T extends Entity> Optional<T> findById(Class<T> clazz, Serializable id, LockModeType lockMode,
			SharedSessionContract session);

	<T extends Entity> Optional<T> findById(Class<T> clazz, Serializable id, SharedSessionContract session);

	<E extends Entity> Optional<Object[]> findOne(Class<E> type, Collection<String> columns, Specification<E> spec);

	<E extends Entity> Optional<Object[]> findOne(Class<E> type, Collection<String> columns, Specification<E> spec,
			LockModeType lockMode);

	<E extends Entity> Optional<Object[]> findOne(Class<E> type, Collection<String> columns, Specification<E> spec,
			LockModeType lockMode, SharedSessionContract session);

	<E extends Entity> Optional<Object[]> findOne(Class<E> type, Collection<String> columns, Specification<E> spec,
			SharedSessionContract session);

	<E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec);

	<E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec, LockModeType lockMode);

	<E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec, LockModeType lockMode,
			SharedSessionContract session);

	<E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec, SharedSessionContract session);

	/* ========================================================================= */

	<T extends Entity> Result<Integer> update(Class<T> type, UpdateQuerySetStatementBuilder<T> setStatementBuilder,
			UpdateQueryWhereStatementBuilder<T> spec);

	<T extends Entity> Result<Integer> update(Class<T> type, UpdateQuerySetStatementBuilder<T> setStatementBuilder,
			UpdateQueryWhereStatementBuilder<T> spec, SharedSessionContract session);

	<T extends Entity, E extends T> Result<E> validate(Class<E> type, Serializable id, E model);

	<T extends Entity, E extends T> Result<E> validate(Class<E> type, Serializable id, E model, Session session);

}
