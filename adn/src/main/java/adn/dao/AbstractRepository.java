/**
 * 
 */
package adn.dao;

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

	protected static final String[] ALL_COLUMNS = new String[0];

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
		Session session = getCurrentSession();
		String hql = resolveSelect(type, ALL_COLUMNS, paging);
		Query<T> query = session.createQuery(hql, type);

		resolveLimit(query, paging);

		return query.getResultList();
	}

	@Override
	public <T extends Entity> List<Object[]> fetch(Class<T> type, String[] columns, Pageable paging) {
		Session session = getCurrentSession();
		String hql = resolveSelect(type, columns, paging);
		Query<Object[]> query = session.createQuery(hql, Object[].class);

		resolveLimit(query, paging);

		return query.getResultList();
	}

	private <T extends Entity> String resolveSelect(Class<T> type, String[] columns, Pageable paging) {
		if (columns.length == 0) {
			// @formatter:off
			return String.format("FROM %s %s",
					EntityUtils.getEntityName(type),
					resolveOrderBy(paging.getSort()));
			// @formatter:on
		}
		// @formatter:off
		return String.format("%s FROM %s %s",
				"SELECT " + Stream.of(columns).collect(Collectors.joining(", ")),
				EntityUtils.getEntityName(type),
				resolveOrderBy(paging.getSort()));
		// @formatter:on
	}

	private String resolveOrderBy(Sort sort) {
		// @formatter:off
		return sort.equals(Sort.unsorted()) ? ""
				: "ORDER BY " +
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
