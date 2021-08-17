/**
 * 
 */
package adn.dao.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.model.entities.Entity;
import adn.service.specification.GenericJpaSpecificationExecutor;
import adn.service.specification.InvalidCriteriaException;
import io.jsonwebtoken.lang.Collections;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class GenericSpecificationExecutorImpl implements GenericJpaSpecificationExecutor {

	private final SessionFactory sessionFactory;

	public GenericSpecificationExecutorImpl(SessionFactory session) {
		super();
		this.sessionFactory = session;
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public <E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query = resolvePredicate(root, query, builder, spec);

		return session.createQuery(query).getResultStream().findFirst();
	}

	@Override
	public <E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query = resolvePredicate(root, query, builder, spec);

		return session.createQuery(query).list();
	}

	@Override
	public <E extends Entity> Page<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = builder.createQuery(type);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);

		Query<E> query = resolvePagedQuery(session, criteriaQuery, pageable);
		List<E> list = query.list();

		return new PageImpl<>(list, pageable, list.size());
	}

	@Override
	public <E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = builder.createQuery(type);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);
		criteriaQuery = resolveSort(root, criteriaQuery, builder, sort);

		return session.createQuery(criteriaQuery).list();
	}

	@Override
	public <E extends Entity> long count(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = resolvePredicate(root, criteriaQuery.select(builder.count(root)), builder, spec);

		return session.createQuery(criteriaQuery).getSingleResult();
	}

	private <E extends Entity> Tuple doFindOne(Session session, Class<E> type, Selections<E> selections,
			Specification<E> spec) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		Root<E> root = query.from(type);

		query.multiselect(resolveSelections(root, selections));
		query = resolvePredicate(root, query, builder, spec);

		return session.createQuery(query).getResultStream().findFirst().orElse(null);
	}

	private <E extends Entity> List<Tuple> doFindAll(Session session, Class<E> type, Selections<E> selections,
			Specification<E> spec) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery.multiselect(resolveSelections(root, selections));
		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);

		return session.createQuery(criteriaQuery).list();
	}

	private <E extends Entity> List<Tuple> doFindAll(Session session, Class<E> type, Selections<E> selections,
			Specification<E> spec, Pageable pageable) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery.multiselect(resolveSelections(root, selections));
		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);

		return resolvePagedQuery(session, criteriaQuery, pageable).list();
	}

	private <E extends Entity> List<Tuple> doFindAll(Session session, Class<E> type, Selections<E> selections,
			Specification<E> spec, Sort sort) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery.multiselect(resolveSelections(root, selections));
		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);
		criteriaQuery = resolveSort(root, criteriaQuery, builder, sort);

		return session.createQuery(criteriaQuery).list();
	}

	private <E> Query<E> resolvePagedQuery(Session session, CriteriaQuery<E> criteriaQuery, Pageable pageable) {
		Query<E> query = session.createQuery(criteriaQuery);

		query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
		query.setMaxResults(pageable.getPageSize());

		return query;
	}

	private <E, R> CriteriaQuery<R> resolveSort(Root<E> root, CriteriaQuery<R> criteriaQuery, CriteriaBuilder builder,
			Sort sort) {
		criteriaQuery.orderBy(
				sort.toList().stream().map(order -> order.isAscending() ? builder.asc(root.get(order.getProperty()))
						: builder.desc(root.get(order.getProperty()))).collect(Collectors.toList()));

		return criteriaQuery;
	}

	private <E, R> CriteriaQuery<R> resolvePredicate(Root<E> root, CriteriaQuery<R> criteriaQuery,
			CriteriaBuilder builder, Specification<E> specification) {
		Predicate predicate = specification.toPredicate(root, criteriaQuery, builder);

		if (predicate == null) {
			throw new IllegalArgumentException(InvalidCriteriaException.INSTANCE);
		}

		return criteriaQuery.where(predicate);
	}

	private <E extends Entity> List<Selection<?>> resolveSelections(Root<E> root, Selections<E> selections) {
		List<Selection<?>> selectionsList = selections.toSelections(root);

		if (Collections.isEmpty(selectionsList)) {
			throw new IllegalArgumentException(InvalidCriteriaException.INSTANCE);
		}

		return selectionsList;
	}

	@Override
	public <E extends Entity> Optional<Tuple> findOne(Class<E> type, Selections<E> selections, Specification<E> spec)
			throws NoSuchFieldException {
		return Optional.ofNullable(doFindOne(getCurrentSession(), type, selections, spec));
	}

	@Override
	public <E extends Entity> List<Tuple> findAll(Class<E> type, Selections<E> selections, Specification<E> spec)
			throws NoSuchFieldException {
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, selections, spec);

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		return tuples;
	}

	@Override
	public <E extends Entity> Page<Tuple> findAll(Class<E> type, Selections<E> selections, Specification<E> spec,
			Pageable pageable) throws NoSuchFieldException {
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, selections, spec, pageable);

		if (tuples.isEmpty()) {
			return Page.empty();
		}

		return new PageImpl<>(tuples, pageable, tuples.size());
	}

	@Override
	public <E extends Entity> List<Tuple> findAll(Class<E> type, Selections<E> selections, Specification<E> spec,
			Sort sort) throws NoSuchFieldException {
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, selections, spec, sort);

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		return tuples;
	}

}
