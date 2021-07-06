/**
 * 
 */
package adn.dao;

import static adn.helpers.ArrayHelper.EMPTY_STRING_ARRAY;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import adn.dao.paging.Unpaged;
import adn.helpers.EntityUtils;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;
import adn.model.specification.Specification;
import adn.model.specification.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractRepository implements Repository {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final SessionFactory sessionFactory;
	protected final SpecificationFactory specificationFactory;

	public AbstractRepository(final SessionFactory sessionFactory, final SpecificationFactory specificationFactory) {
		this.sessionFactory = sessionFactory;
		this.specificationFactory = specificationFactory;
	}

	@Override
	public <T extends Entity> T findById(Serializable id, Class<T> clazz) {
		return sessionFactory.getCurrentSession().get(clazz, id);
	}

	@Override
	public <T extends Entity> T findOne(CriteriaQuery<T> query, Class<T> clazz) {
		// @formatter:off
		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultStream().findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public <T extends Entity> List<T> find(CriteriaQuery<T> query, Class<T> clazz) {
		// @formatter:off
		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
		// @formatter:on
	}

	@Override
	public <T extends Entity> List<T> fetch(Class<T> clazz) {
		return fetch(clazz, Unpaged.INSTANCE);
	}

	@Override
	public <T extends Entity> List<T> fetch(Class<T> type, Pageable paging) {
		return fetch(type, paging, EMPTY_STRING_ARRAY);
	}

	@Override
	public <T extends Entity> List<T> fetch(Class<T> type, Pageable paging, String[] groupByColumns) {
		Session session = getCurrentSession();
		String hql = resolveFetchQuery(type, EMPTY_STRING_ARRAY, paging, groupByColumns);
		Query<T> query = session.createQuery(hql, type);

		resolveLimit(query, paging);

		return query.getResultList();
	}

	@Override
	public <T extends Entity> List<Object[]> fetch(Class<T> type, String[] columns, Pageable paging) {
		return fetch(type, columns, paging, EMPTY_STRING_ARRAY);
	}

	@Override
	public <T extends Entity> List<Object[]> fetch(Class<T> type, String[] columns, Pageable paging,
			String[] groupByColumns) {
		Session session = getCurrentSession();
		String hql = resolveFetchQuery(type, columns, paging, groupByColumns);
		Query<Object[]> query = session.createQuery(hql, Object[].class);

		resolveLimit(query, paging);

		return query.getResultList();
	}

	private <T extends Entity> String resolveFetchQuery(Class<T> type, String[] columns, Pageable paging,
			String[] groupByColumns) {
		// @formatter:off
		return appendOrderBy(
					appendGroupBy(
						resolveSelect(type, columns),
						groupByColumns),
					paging.getSort());
		// @formatter:on
	}

	private <T> String appendGroupBy(String queryString, String[] groupByColumns) {
		if (groupByColumns.length == 0) {
			return queryString;
		}

		return queryString + " GROUP BY " + Stream.of(groupByColumns).collect(Collectors.joining(", "));
	}

	private <T extends Entity> String resolveSelect(Class<T> type, String[] columns) {
		// @formatter:off
		if (columns.length == 0) {
			return String.format("FROM %s",
					EntityUtils.getEntityName(type));
		}
		
		return String.format("%s FROM %s",
				"SELECT " + Stream.of(columns).collect(Collectors.joining(", ")),
				EntityUtils.getEntityName(type));
		// @formatter:on
	}

	private String appendOrderBy(String queryString, Sort sort) {
		// @formatter:off
		return sort.equals(Sort.unsorted()) ? queryString
				: queryString + " ORDER BY " +
					sort.map(this::fromOrder).stream()
						.collect(Collectors.joining(", "));
		// @formatter:on
	}

	private <T> Query<T> resolveLimit(Query<T> query, Pageable paging) {
		// @formatter:off
		query
			.setFirstResult(paging.getPageNumber() * paging.getPageSize())
			.setMaxResults(paging.getPageSize());
		// @formatter:on
		return query;
	}

	private String fromOrder(Order order) {
		return order.getProperty() + " " + order.getDirection();
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected <T extends Entity, E extends T> Specification<E> getSpecification(Class<E> type) {
		return specificationFactory.getSpecification(type);
	}

	protected <T extends Entity, E extends T> DatabaseInteractionResult<E> validate(Serializable id, E instance,
			Class<E> type) {
		Specification<E> spec = getSpecification(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Validating [%s#%s] using [%s]", type.getName(), id, spec.getClass().getName()));
		}

		return spec.isSatisfiedBy(id, instance);
	}

}