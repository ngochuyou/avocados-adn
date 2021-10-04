/**
 * 
 */
package adn.dao.generic;

import static adn.helpers.HibernateHelper.getIdentifierPropertyName;
import static adn.helpers.HibernateHelper.toRows;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.Query;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.AssociationType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.application.context.builders.EntityBuilderProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.application.context.builders.ValidatorFactory;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.HibernateHelper;
import adn.model.DomainEntity;
import adn.model.entities.Entity;
import adn.model.entities.PermanentEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.entities.metadata._PermanentEntity;
import adn.model.entities.validator.Validator;
import adn.service.internal.InvalidCriteriaException;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Repository
@Primary
public class GenericRepositoryImpl implements GenericRepository, ContextBuilder {

	private static final Logger logger = LoggerFactory.getLogger(GenericRepositoryImpl.class);

	private final SessionFactory sessionFactory;
	private final ValidatorFactory validatorFactory;
	private final ModelContextProvider modelContext;
	private final EntityBuilderProvider entityBuilderProvider;

	private Map<Class<? extends Entity>, Map<String, BiFunction<String, Root<? extends Entity>, Path<?>>>> selectorMap;
	private Map<Class<? extends Entity>, BiFunction<Root<? extends Entity>, CriteriaBuilder, Predicate>> mandatoryPredicateMap;

	@Autowired
	public GenericRepositoryImpl(SessionFactory sessionFactory, ValidatorFactory specificationFactory,
			ModelContextProvider modelContext, EntityBuilderProvider entityBuilderProvider) {
		this.sessionFactory = sessionFactory;
		this.validatorFactory = specificationFactory;
		this.modelContext = modelContext;
		this.entityBuilderProvider = entityBuilderProvider;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		logger.info(String.format("Configuring %s", this.getClass().getName()));
		Map<Class<? extends Entity>, Map<String, BiFunction<String, Root<? extends Entity>, Path<?>>>> selectorMap = new HashMap<>(
				0, 1f);
		Map<Class<? extends Entity>, BiFunction<Root<? extends Entity>, CriteriaBuilder, Predicate>> mandatoryPredicateMap = new HashMap<>(
				0, 1f);

		modelContext.getEntityTree().forEach(tree -> {
			Class<? extends DomainEntity> entityType = tree.getNode();

			if (!Entity.class.isAssignableFrom(entityType) || Modifier.isAbstract(entityType.getModifiers())) {
				logger.debug(String.format("Skiping non-Entity of type [%s]", entityType.getName()));
				return;
			}

			Class<? extends Entity> type = (Class<? extends Entity>) entityType;
			Map<String, BiFunction<String, Root<? extends Entity>, Path<?>>> selectors = new HashMap<>();
			DomainEntityMetadata<? extends Entity> metadata = modelContext.getMetadata(type);
			EntityPersister persister = HibernateHelper.getEntityPersister(type);
			EntityMetamodel metamodel = persister.getEntityMetamodel();
			List<String> propertyNames = Stream.of(metamodel.getPropertyNames()).collect(Collectors.toList());
			String identifierName = metamodel.getIdentifierProperty().getName();

			propertyNames.add(identifierName);
			propertyNames.stream().forEach(propName -> {
				Type propertyType = propName.equals(identifierName) ? metamodel.getIdentifierProperty().getType()
						: metamodel.getPropertyTypes()[metamodel.getPropertyIndex(propName)];

				if (propertyType instanceof ComponentType) {
					selectors.putAll(resolveComponentTypeSelectors(type, null, propName, (ComponentType) propertyType,
							metadata, new ArrayList<>()));
					return;
				}

				if (metadata.isAssociationOptional(propName)) {
					logger.debug(String.format("Optional association: [%s] in type [%s]", propName, type.getName()));
					selectors.put(propName, (column, root) -> root.join(column, JoinType.LEFT));
					return;
				}

				selectors.put(propName, (column, root) -> root.get(column));
			});

			selectorMap.put(type, selectors);
			mandatoryPredicateMap.put(type,
					(root, builder) -> PermanentEntity.class.isAssignableFrom(type)
							? builder.isTrue(root.get(_PermanentEntity.active))
							: builder.conjunction());
		});

		this.selectorMap = Collections.unmodifiableMap(selectorMap);
		this.mandatoryPredicateMap = Collections.unmodifiableMap(mandatoryPredicateMap);
		logger.info(String.format("Finished %s", this.getClass().getName()));
	}

	// @formatter:off
	private Map<String, BiFunction<String, Root<? extends Entity>, Path<?>>> resolveComponentTypeSelectors(
			Class<?> owningType,
			String owningPropertyName,
			String propertyName,
			ComponentType type,
			DomainEntityMetadata<? extends Entity> metadata,
			List<String> joinPath) {
		logger.debug(String.format("Embedded component: [%s] in type [%s]", propertyName, owningType.getName()));

		Map<String, BiFunction<String, Root<? extends Entity>, Path<?>>> selectors = new HashMap<>();
		// @formatter:off
		selectors.put(propertyName,
				owningPropertyName == null ? 
					(column, root) -> root.get(column) :
					(column, root) -> selectors.get(owningPropertyName).apply(owningPropertyName, root).get(column));
		joinPath.add(propertyName);
		// @formatter:on
		Stream.of(type.getPropertyNames()).forEach(subProp -> {
			Type subType = type.getSubtypes()[type.getPropertyIndex(subProp)];

			if (subType instanceof ComponentType) {
				selectors.putAll(resolveComponentTypeSelectors(subType.getReturnedClass(), propertyName, subProp,
						(ComponentType) subType, metadata, joinPath));
				return;
			}

			if (subType instanceof AssociationType && metadata.isAssociationOptional(subProp)) {
				logger.debug(String.format("Optional association: [%s] in type [%s]", subProp, type.getName()));
				selectors.put(subProp, (column, root) -> {
					Iterator<String> iterator = joinPath.iterator();
					Join<?, ?> join = root.join(iterator.next());

					while (iterator.hasNext()) {
						join = join.join(iterator.next());
					}

					return join.join(subProp, JoinType.LEFT);
				});
				return;
			}

			selectors.put(subProp, (column, root) -> selectors.get(propertyName).apply(propertyName, root).get(column));
		});

		return selectors;
	}

	// @formatter:on
	@Override
	public <T extends Entity> Optional<T> findById(Class<T> clazz, Serializable id) {
		return Optional.ofNullable(getCurrentSession().get(clazz, id));
	}

	@SuppressWarnings("serial")
	private static <T extends Entity> Specification<T> hasId(Class<T> type, Serializable id) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				return builder.equal(root.get(getIdentifierPropertyName(type)), id);
			}
		};
	}

	@Override
	public <T extends Entity> Optional<Object[]> findById(Class<T> type, Serializable id, Collection<String> columns) {
		return findOne(type, columns, hasId(type, id));
	}

	public <T extends Entity> List<Selection<?>> resolveSelect(Class<T> type, Root<T> root,
			Collection<String> columns) {
		Map<String, BiFunction<String, Root<? extends Entity>, Path<?>>> selectors = selectorMap.get(type);
		// there must not be any null selector retrieved from the selectors or else
		// there's a system fraud
		return columns.stream().map(column -> selectors.get(column).apply(column, root)).collect(Collectors.toList());
	}

	@Override
	public <T extends Entity> List<T> findAll(Class<T> type, Pageable paging) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(type);
		Root<T> root = cq.from(type);

		cq.where(resolvePredicate(type, root, cq, builder, null));

		Query<T> hql = resolveFetchQuery(session, cq, root, builder, paging);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.list();
	}

	private <T extends Entity, E> Query<E> resolveFetchQuery(Session session, CriteriaQuery<E> query, Root<T> root,
			CriteriaBuilder builder, Pageable paging) {
		return resolvePagedQuery(session, query.orderBy(resolveSort(root, builder, paging.getSort())), paging);
	}

	@Override
	public <T extends Entity> List<Object[]> findAll(Class<T> type, Collection<String> columns, Pageable paging) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<T> root = cq.from(type);

		cq.multiselect(resolveSelect(type, root, columns)).where(resolvePredicate(type, root, cq, builder, null));

		Query<Tuple> hql = resolveFetchQuery(session, cq, root, builder, paging);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return toRows(hql.list());
	}

	@Override
	public <T extends Entity> long count(Class<T> type) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<T> root = criteriaQuery.from(type);

		criteriaQuery.select(builder.count(root)).where(resolvePredicate(type, root, criteriaQuery, builder, null));

		return session.createQuery(criteriaQuery).getSingleResult();
	}

	@Override
	public <T extends Entity> long countById(Class<T> type, Serializable id) {
		return count(type, hasId(type, id));
	}

	Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	<T extends Entity, E extends T> Result<E> validate(Session session, Serializable id, E instance, Class<E> type) {
		Validator<E> validator = validatorFactory.getValidator(type);

		if (logger.isDebugEnabled()) {
			logger.debug(
					String.format("Validating [%s#%s] using [%s]", type.getName(), id, validator.getLoggableName()));
		}

		return validator.isSatisfiedBy(session, id, instance);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> insert(Class<E> type, Serializable id, E persistence) {
		return insert(type, id, persistence, getCurrentSession());
	}

	@Override
	public <T extends Entity, E extends T> Result<E> insert(Class<E> type, Serializable id, E persistence,
			Session session) {
		// validate the persisted entity
		Result<E> result = validate(session, id, persistence, type);

		if (result.isOk()) {
			persistence = entityBuilderProvider.getBuilder(type).buildPostValidationOnInsert(id, persistence);
			session.save(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Class<E> type, Serializable id, E persistence) {
		return update(type, id, persistence, getCurrentSession());
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Class<E> type, Serializable id, E persistence,
			Session session) {
		Result<E> result = validate(session, id, persistence, type);

		if (result.isOk()) {
			session.update(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	@Override
	public <E extends Entity> Optional<E> findOne(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query.where(resolvePredicate(type, root, query, builder, spec));

		Query<E> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.getResultStream().findFirst();
	}

	@Override
	public <E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query.where(resolvePredicate(type, root, query, builder, spec));

		Query<E> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.list();
	}

	@Override
	public <E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query.where(resolvePredicate(type, root, query, builder, spec));

		Query<E> hql = resolvePagedQuery(session, query, pageable);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.list();
	}

	@Override
	public <E extends Entity> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query.where(resolvePredicate(type, root, query, builder, spec)).orderBy(resolveSort(root, builder, sort));

		Query<E> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.list();
	}

	@Override
	public <E extends Entity> long count(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<E> root = query.from(type);

		query.select(builder.count(root)).where(resolvePredicate(type, root, query, builder, spec));

		Query<Long> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.getSingleResult();
	}

	@Override
	public <E extends Entity> Optional<Object[]> findOne(Class<E> type, Collection<String> columns,
			Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<E> root = query.from(type);

		query.multiselect(resolveSelect(type, root, columns)).where(resolvePredicate(type, root, query, builder, spec));

		Query<Tuple> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql.getResultStream().findFirst().map(Tuple::toArray);
	}

	@Override
	public <E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		Root<E> root = query.from(type);

		query.multiselect(resolveSelect(type, root, columns)).where(resolvePredicate(type, root, query, builder, spec));

		Query<Tuple> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return toRows(hql.list());
	}

	@Override
	public <E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Pageable pageable) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		Root<E> root = query.from(type);

		query.multiselect(resolveSelect(type, root, columns)).where(resolvePredicate(type, root, query, builder, spec));

		Query<Tuple> hql = resolvePagedQuery(session, query, pageable);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return toRows(hql.list());
	}

	@Override
	public <E extends Entity> List<Object[]> findAll(Class<E> type, Collection<String> columns, Specification<E> spec,
			Sort sort) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		Root<E> root = query.from(type);

		query.multiselect(resolveSelect(type, root, columns)).where(resolvePredicate(type, root, query, builder, spec))
				.orderBy(resolveSort(root, builder, sort));

		Query<Tuple> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return toRows(hql.list());
	}

	public <E> Query<E> resolvePagedQuery(Session session, CriteriaQuery<E> criteriaQuery, Pageable pageable) {
		Query<E> query = session.createQuery(criteriaQuery);

		query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
		query.setMaxResults(pageable.getPageSize());

		return query;
	}

	private <E> List<Order> resolveSort(Root<E> root, CriteriaBuilder builder, Sort sort) {
		return sort.stream().map(order -> order.isAscending() ? builder.asc(root.get(order.getProperty()))
				: builder.desc(root.get(order.getProperty()))).collect(Collectors.toList());
	}

	private <E extends Entity, R> Predicate resolvePredicate(Class<E> entityType, Root<E> root,
			CriteriaQuery<R> criteriaQuery, CriteriaBuilder builder, Specification<E> specification) {
		return builder.and(mandatoryPredicateMap.get(entityType).apply(root, builder),
				resolveRequestedPredicate(root, criteriaQuery, builder, specification));
	}

	private <E, R> Predicate resolveRequestedPredicate(Root<E> root, CriteriaQuery<R> criteriaQuery,
			CriteriaBuilder builder, Specification<E> specification) {
		if (specification == null) {
			return builder.conjunction();
		}

		Predicate predicate = specification.toPredicate(root, criteriaQuery, builder);

		if (predicate == null) {
			throw new IllegalArgumentException(InvalidCriteriaException.INSTANCE);
		}

		return predicate;
	}

}
