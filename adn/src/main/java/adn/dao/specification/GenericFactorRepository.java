/**
 * 
 */
package adn.dao.specification;

import static adn.helpers.HibernateHelper.selectColumns;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.helpers.HibernateHelper;
import adn.model.entities.Factor;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class GenericFactorRepository {

	private final SessionFactory sessionFactory;

	public GenericFactorRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected <E> Predicate isActive(CriteriaBuilder builder, Root<E> root) {
		return builder.isTrue(root.get(Factor.ACTIVE_FIELD_NAME));
	}

	public <T extends Factor, E extends T> Long countActive(Class<E> type) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<E> root = query.from(type);

		query.select(builder.count(root)).where(isActive(builder, root));

		return session.createQuery(query).getSingleResult();
	}

	public <T extends Factor, E extends T> Tuple findActive(Class<E> type, Serializable id, Collection<String> columns)
			throws NoSuchFieldException {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		Root<E> root = query.from(type);

		query = selectColumns(query, root, columns).where(builder.and(isActive(builder, root),
				builder.equal(root.get(HibernateHelper.getIdentifierPropertyName(type)), id)));

		return session.createQuery(query).getResultStream().findFirst().orElse(null);
	}

	protected <R> Query<R> resolvePaging(Session session, CriteriaQuery<R> criteriaQuery, Pageable paging) {
		Query<R> query = session.createQuery(criteriaQuery);

		query.setFirstResult(paging.getPageNumber() * paging.getPageSize());
		query.setMaxResults(paging.getPageSize());

		return query;
	}

	public <T extends Factor, E extends T> List<Tuple> findAllActive(Class<E> type, Collection<String> columns,
			Pageable paging) throws NoSuchFieldException {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = selectColumns(criteriaQuery, root, columns).where(isActive(builder, root));

		Query<Tuple> query = resolvePaging(session, criteriaQuery, paging);

		return query.list();
	}

	public <T extends Factor, E extends T> List<Tuple> findAllActive(Class<E> type, Collection<String> columns,
			Pageable paging, Specification<E> extraSpec) throws NoSuchFieldException {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = extraSpec != null
				? selectColumns(criteriaQuery, root, columns).where(
						builder.and(isActive(builder, root), extraSpec.toPredicate(root, criteriaQuery, builder)))
				: selectColumns(criteriaQuery, root, columns).where(isActive(builder, root));

		return resolvePaging(session, criteriaQuery, paging).list();
	}

}
