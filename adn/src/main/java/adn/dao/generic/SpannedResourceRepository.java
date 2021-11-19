/**
 * 
 */
package adn.dao.generic;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.springframework.data.jpa.domain.Specification;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface SpannedResourceRepository<T extends Temporal, E extends Entity> {

	Optional<E> findCurrent(Specification<E> specification);
	
	Optional<E> findCurrent(Specification<E> specification, Session session);

	Optional<Object[]> findCurrent(Collection<String> columns, Specification<E> specification);

	Optional<Object[]> findCurrent(Collection<String> columns, Specification<E> specification, Session session);
	
	List<E> findAllCurrents(Specification<E> specification);

	List<E> findAllCurrents(Specification<E> specification, Session session);
	
	List<Object[]> findAllCurrents(Collection<String> columns, Specification<E> specification);

	List<Object[]> findAllCurrents(Collection<String> columns, Specification<E> specification, Session session);
	
	List<E> findAllCurrents(String specification, Object... parameterValues);

	List<E> findAllCurrents(String specification, Session session, Object... parameterValues);
	
	List<Object[]> findAllCurrents(Collection<String> columns, String specification, Object... parameterValues);
	
	List<Object[]> findAllCurrents(Collection<String> columns, String specification, Session session, Object... parameterValues);

	Optional<E> findOverlapping(Specification<E> specification, T appliedTimestamp, T droppedTimestamp);

	Optional<E> findOverlapping(Specification<E> specification, T appliedTimestamp, T droppedTimestamp, Session session);
	
	Optional<Object[]> findOverlapping(Collection<String> columns, Specification<E> specification, T appliedTimestamp,
			T droppedTimestamp);
	
	Optional<Object[]> findOverlapping(Collection<String> columns, Specification<E> specification, T appliedTimestamp,
			T droppedTimestamp, Session session);

}
