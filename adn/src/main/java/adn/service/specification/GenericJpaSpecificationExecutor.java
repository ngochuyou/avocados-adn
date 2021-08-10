/**
 * 
 */
package adn.service.specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericJpaSpecificationExecutor {

	<E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec);

	<E extends Entity> Page<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable);

	<E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort);

	<E extends Entity> long count(Class<E> type, Specification<E> spec);

	<E extends Entity> Optional<Tuple> findOne(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec) throws NoSuchFieldException;

	<E extends Entity> List<Tuple> findAll(Class<E> type, Collection<String> requestedColumns, Specification<E> spec)
			throws NoSuchFieldException;

	<E extends Entity> Page<Tuple> findAll(Class<E> type, Collection<String> requestedColumns, Specification<E> spec,
			Pageable pageable) throws NoSuchFieldException;

	<E extends Entity> List<Tuple> findAll(Class<E> type, Collection<String> requestedColumns, Specification<E> spec,
			Sort sort) throws NoSuchFieldException;

}
