/**
 * 
 */
package adn.dao.generic;

import static adn.helpers.CollectionHelper.EMPTY_STRING_ARRAY;
import static adn.helpers.HibernateHelper.getEntityName;
import static adn.helpers.HibernateHelper.getIdentifierPropertyName;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import adn.helpers.Utils.Entry;
import adn.model.entities.Entity;
import adn.model.specification.Specification;
import adn.model.specification.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Repository
@Primary
public class GenericRepository implements Repository {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final SessionFactory sessionFactory;
	private final SpecificationFactory specificationFactory;
	// @formatter:off
	private static final Map<ParamType, BiConsumer<Query<?>, Entry<String, Object>>> PARAM_CONTEXT_RESOLVER = Map.of(
			ParamType.SINGULAR, (hql, entry) -> hql.setParameter(entry.getKey(), entry.getValue()),
			ParamType.PLURAL, (hql, entry) -> hql.setParameterList(entry.getKey(), (Collection<?>) entry.getValue()),
			ParamType.ARRAY, (hql, entry) -> hql.setParameterList(entry.getKey(), (Object[]) entry.getValue())
			);
	// @formatter:on
	public GenericRepository(final SessionFactory sessionFactory, final SpecificationFactory specificationFactory) {
		this.sessionFactory = sessionFactory;
		this.specificationFactory = specificationFactory;
	}

	@Override
	public <T extends Entity> T findById(Serializable id, Class<T> clazz) {
		return getCurrentSession().get(clazz, id);
	}

	private Object[] resolveResult(Object result) {
		if (result == null) {
			return null;
		}

		return result.getClass().isArray() ? (Object[]) result : new Object[] { result };
	}

	private Query<?> resolveQueryByColumns(String query, String[] columns) {
		Session ss = getCurrentSession();

		return columns.length == 1 ? ss.createQuery(query, Object.class) : ss.createQuery(query, Object[].class);
	}

	@Override
	public <T extends Entity> Object[] findById(Serializable id, Class<T> type, String[] columns) {
		// @formatter:off
		String query = String.format("%s WHERE e.%s=:id",
				resolveSelect(type, columns),
				getIdentifierPropertyName(type));
		// @formatter:on
		Query<?> hql = resolveQueryByColumns(query, columns);

		hql.setParameter("id", id);

		Object result = hql.getResultStream().findFirst().orElse(null);

		return resolveResult(result);
	}

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

	private Query<?> resolveHQL(String query) {
		return getCurrentSession().createQuery(query);
	}

	private Query<?> resolveHQLParams(String query, Map<String, Object> parameters) {
		Query<?> hql = resolveHQL(query);

		for (Map.Entry<String, Object> param : parameters.entrySet()) {
			hql.setParameter(param.getKey(), param.getValue());
		}

		return hql;
	}

	private Query<?> resolveHQLParamContexts(String query, Map<String, ParamContext> contexts) {
		Query<?> hql = resolveHQL(query);
		ParamContext param;

		for (Map.Entry<String, ParamContext> contextEntry : contexts.entrySet()) {
			param = contextEntry.getValue();
			PARAM_CONTEXT_RESOLVER.get(param.getType()).accept(hql,
					Entry.entry(contextEntry.getKey(), param.getValue()));
		}

		return hql;
	}

	@Override
	public Object[] findOne(String query, Map<String, Object> parameters) {
		Query<?> hql = resolveHQLParams(query, parameters);

		hql.setMaxResults(1);

		return resolveResult(hql.getResultStream().findFirst().orElse(null));
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<?> nativelyFind(String query) {
		Session session = getCurrentSession();
		NativeQuery nativeQuery = session.createSQLQuery(query);

		return nativeQuery.list();
	}

	@Override
	public List<?> find(String query, Map<String, Object> parameters) {
		return resolveHQLParams(query, parameters).getResultList();
	}

	@Override
	public List<?> find(String query, Pageable paging, Map<String, Object> parameters) {
		return resolveLimit(resolveHQLParams(query, parameters), paging).getResultList();
	}

	@Override
	public List<?> findWithContext(String query, Map<String, ParamContext> parameters) {
		return resolveHQLParamContexts(query, parameters).getResultList();
	}

	@Override
	public List<Object[]> findWithContext(String query, Pageable paging, Map<String, ParamContext> parameters) {
		return null;
	}

	@Override
	public <T extends Entity> List<T> fetch(Class<T> type, Pageable paging) {
		Session session = getCurrentSession();
		String hql = resolveFetchQuery(type, EMPTY_STRING_ARRAY, paging);
		Query<T> query = session.createQuery(hql, type);

		resolveLimit(query, paging);

		return query.getResultList();
	}

	@Override
	public <T extends Entity> List<Object[]> fetch(Class<T> type, String[] columns, Pageable paging) {
		Session session = getCurrentSession();
		String hql = resolveFetchQuery(type, columns, paging);
		Query<Object[]> query = session.createQuery(hql, Object[].class);

		resolveLimit(query, paging);

		return query.getResultList();
	}

	private <T extends Entity> String resolveFetchQuery(Class<T> type, String[] columns, Pageable paging) {
		return appendOrderBy(resolveSelect(type, columns), paging.getSort());
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
			return String.format("FROM %s e",
					getEntityName(type));
		}
		
		return String.format("%s FROM %s e",
				"SELECT " + Stream.of(columns).map(col -> "e." + col).collect(Collectors.joining(", ")),
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

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> count(String hql, Map<String, Object> params) {
		return (List<Long>) resolveHQLParams(hql, params).getResultList();
	}

	@Override
	public <T extends Entity> Long count(Class<T> type) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<T> root = criteriaQuery.from(type);

		criteriaQuery.select(builder.count(root));

		return session.createQuery(criteriaQuery).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> countWithContext(String hql, Map<String, ParamContext> params) {
		return (List<Long>) resolveHQLParamContexts(hql, params).getResultList();
	}

	@Override
	public <T extends Entity> Long countById(Serializable id, Class<T> type) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<T> root = criteriaQuery.from(type);

		criteriaQuery.select(builder.count(root));
		criteriaQuery.where(builder.equal(root.get(getIdentifierPropertyName(type)), id));

		return session.createQuery(criteriaQuery).getSingleResult();
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected <T extends Entity, E extends T> Specification<E> getSpecification(Class<E> type) {
		return specificationFactory.getSpecification(type);
	}

	protected <T extends Entity, E extends T> Result<E> validate(Session session, Serializable id, E instance,
			Class<E> type) {
		Specification<E> spec = getSpecification(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Validating [%s#%s] using [%s]", type.getName(), id, spec.getClass().getName()));
		}

		return spec.isSatisfiedBy(session, id, instance);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> insert(Serializable id, E persistence, Class<E> type) {
		return insert(getCurrentSession(), id, persistence, type);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> insert(Session session, Serializable id, E persistence,
			Class<E> type) {
		// validate the persisted entity
		Result<E> result = validate(session, id, persistence, type);

		if (result.isOk()) {
			session.save(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Serializable id, E persistence, Class<E> type) {
		return update(getCurrentSession(), id, persistence, type);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Session session, Serializable id, E persistence,
			Class<E> type) {
		Result<E> result = validate(session, id, persistence, type);

		if (result.isOk()) {
			session.update(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

}
