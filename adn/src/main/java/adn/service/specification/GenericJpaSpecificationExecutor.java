/**
 * 
 */
package adn.service.specification;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import adn.model.entities.Entity;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericJpaSpecificationExecutor<T extends Entity> extends JpaSpecificationExecutor<T> {

	<E extends T> Optional<E> findOne(Class<E> type, Specification<E> spec);

	<E extends T> List<E> findAll(Class<E> type, Specification<E> spec);

	<E extends T> Page<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable);

	<E extends T> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort);

	<E extends T> long count(Class<E> type, Specification<E> spec);

	<E extends T> Optional<Map<String, Object>> findOne(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException;

	<E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException;

	<E extends T> Page<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Pageable pageable, Role role) throws NoSuchFieldException;

	<E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Sort sort, Role role) throws NoSuchFieldException;
	
	<E extends T> Optional<Map<String, Object>> findOne(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException;

	<E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException;

	<E extends T> Page<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Pageable pageable, UUID departmentId) throws NoSuchFieldException;

	<E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Sort sort, UUID departmentId) throws NoSuchFieldException;

}
