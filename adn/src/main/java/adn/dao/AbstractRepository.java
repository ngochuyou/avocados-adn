/**
 * 
 */
package adn.dao;

import static adn.helpers.ArrayHelper.EMPTY_STRING_ARRAY;
import static adn.helpers.EntityUtils.getEntityName;
import static adn.helpers.EntityUtils.getIdentifierPropertyName;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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
import adn.dao.parameter.ParamContext;
import adn.dao.parameter.ParamType;
import adn.helpers.Utils.Entry;
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
	// @formatter:off
	protected final Map<ParamType, BiConsumer<Query<?>, Entry<String, Object>>> paramContextResolvers = Map.of(
			ParamType.SINGULAR, (hql, entry) -> hql.setParameter(entry.getKey(), entry.getValue()),
			ParamType.PLURAL, (hql, entry) -> hql.setParameterList(entry.getKey(), (Collection<?>) entry.getValue()),
			ParamType.ARRAY, (hql, entry) -> hql.setParameterList(entry.getKey(), (Object[]) entry.getValue())
			);
	// @formatter:on
	public AbstractRepository(final SessionFactory sessionFactory, final SpecificationFactory specificationFactory) {
		this.sessionFactory = sessionFactory;
		this.specificationFactory = specificationFactory;
	}

	@Override
	public <T extends Entity> T findById(Serializable id, Class<T> clazz) {
		return getCurrentSession().get(clazz, id);
	}

	@Override
	public <T extends Entity> Object[] findById(Serializable id, Class<T> clazz, String[] columns) {
		// @formatter:off
		String query = String.format("%s WHERE %s=:id",
				resolveSelect(clazz, columns),
				getIdentifierPropertyName(clazz));
		// @formatter:on
		Session session = getCurrentSession();

		if (columns.length == 1) {
			Query<Object> hql = session.createQuery(query, Object.class);

			hql.setParameter("id", id);

			return new Object[] { hql.getSingleResult() };
		}

		Query<Object[]> hql = session.createQuery(query, Object[].class);

		hql.setParameter("id", id);

		return hql.getSingleResult();
	}

//	@Override
//	@SuppressWarnings("unchecked")
//	public <T extends Entity> T findById(Serializable id, Class<T> clazz, String[] columns, boolean persistOnFinish) {
//		Session session = getCurrentSession();
//		Query<Object[]> hql = session.createQuery(
//				String.format("%s WHERE %s=:id", resolveSelect(clazz, columns), getIdentifierPropertyName(clazz)),
//				Object[].class);
//		Object[] row = hql.getSingleResult();
//
//		if (row == null) {
//			return null;
//		}
//
//		EntityTuplizer tuplizer = getEntityPersister(clazz).getEntityTuplizer();
//		T instance = (T) tuplizer.instantiate(id, session.unwrap(SharedSessionContractImplementor.class));
//		int i = 0;
//
//		for (String prop : columns) {
//			tuplizer.setPropertyValue(instance, prop, row[i++]);
//		}
//
//		if (persistOnFinish) {
//			session.persist(instance);
//		}
//
//		return instance;
//	}

	@Override
	public <T extends Entity> T findOne(CriteriaQuery<T> query, Class<T> clazz) {
		// @formatter:off
		return getCurrentSession()
				.createQuery(query)
				.setMaxResults(1)
				.getResultStream().findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public <T extends Entity> T findOne(String query, Class<T> type, Map<String, Object> parameters) {
		Session session = getCurrentSession();
		Query<T> hql = session.createQuery(query, type);

		for (Map.Entry<String, Object> param : parameters.entrySet()) {
			hql.setParameter(param.getKey(), param.getValue());
		}

		hql.setMaxResults(1);

		return hql.getResultStream().findFirst().orElse(null);
	}

	private <T> Query<T> resolveHQL(String query, Class<T> type) {
		return getCurrentSession().createQuery(query, type);
	}

	private <T> Query<T> resolveHQLParams(String query, Class<T> type, Map<String, Object> parameters) {
		Query<T> hql = resolveHQL(query, type);

		for (Map.Entry<String, Object> param : parameters.entrySet()) {
			hql.setParameter(param.getKey(), param.getValue());
		}

		return hql;
	}

	private <T> Query<T> resolveHQLParamContexts(String query, Class<T> type, Map<String, ParamContext> contexts) {
		Query<T> hql = resolveHQL(query, type);
		ParamContext param;

		for (Map.Entry<String, ParamContext> contextEntry : contexts.entrySet()) {
			param = contextEntry.getValue();
			paramContextResolvers.get(param.getType()).accept(hql,
					Entry.entry(contextEntry.getKey(), param.getValue()));
		}

		return hql;
	}

	@Override
	public Object[] findOne(String query, Map<String, Object> parameters) {
		Query<Object[]> hql = resolveHQLParams(query, Object[].class, parameters);

		hql.setMaxResults(1);

		return hql.getResultStream().findFirst().orElse(null);
	}

	@Override
	public <T extends Entity> List<T> find(CriteriaQuery<T> query, Class<T> clazz) {
		// @formatter:off
		return getCurrentSession()
				.createQuery(query)
				.getResultList();
		// @formatter:on
	}

	@Override
	public List<Object[]> find(String query, Map<String, Object> parameters) {
		return resolveHQLParams(query, Object[].class, parameters).getResultList();
	}

	@Override
	public List<Object[]> find(String query, Pageable paging, Map<String, Object> parameters) {
		return resolveLimit(resolveHQLParams(query, Object[].class, parameters), paging).getResultList();
	}

	@Override
	public List<Object[]> findWithContext(String query, Map<String, ParamContext> parameters) {
		Query<Object[]> hql = resolveHQLParamContexts(query, Object[].class, parameters);

		return hql.getResultList();
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

	public <T> String appendGroupBy(String queryString, String[] groupByColumns) {
		if (groupByColumns.length == 0) {
			return queryString;
		}

		return queryString + " GROUP BY " + Stream.of(groupByColumns).collect(Collectors.joining(", "));
	}

	private <T extends Entity> String resolveSelect(Class<T> type, String[] columns) {
		// @formatter:off
		if (columns.length == 0) {
			return String.format("FROM %s",
					getEntityName(type));
		}
		
		return String.format("%s FROM %s",
				"SELECT " + Stream.of(columns).collect(Collectors.joining(", ")),
				getEntityName(type));
		// @formatter:on
	}

	public String appendOrderBy(String queryString, Sort sort) {
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

	@Override
	public List<Long> count(String hql, Map<String, Object> params) {
		return resolveHQLParams(hql, Long.class, params).getResultList();
	}

	@Override
	public <T extends Entity> Long count(Class<T> type) {
		Session session = getCurrentSession();
		Query<Long> hql = session.createQuery(String.format("SELECT COUNT(*) FROM %s", getEntityName(type)),
				Long.class);

		return hql.getSingleResult();
	}

	@Override
	public List<Long> countWithContext(String hql, Map<String, ParamContext> params) {
		return resolveHQLParamContexts(hql, Long.class, params).getResultList();
	}

	@Override
	public <T extends Entity> Long countById(Serializable id, Class<T> type) {
		Session session = getCurrentSession();
		// @formatter:off
		Query<Long> hql = session.createQuery(String.format("SELECT COUNT(*) FROM %s WHERE %s=:id",
				getEntityName(type),
				getIdentifierPropertyName(type)),
				Long.class);
		// @formatter:on

		hql.setParameter("id", id);

		return hql.getSingleResult();
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
