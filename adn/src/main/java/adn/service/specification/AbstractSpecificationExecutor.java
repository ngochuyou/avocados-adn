/**
 * 
 */
package adn.service.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;

import adn.model.DepartmentScoped;
import adn.model.entities.Entity;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.internal.CRUDService;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractSpecificationExecutor<T extends Entity> implements GenericJpaSpecificationExecutor<T> {

	private final SessionFactory sessionFactory;
	private final CRUDService crudService;
	protected final AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory;
	protected final DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory;

	private static final Optional<Map<String, Object>> EMPTY_PROPERTIES = Optional.ofNullable(null);

	public AbstractSpecificationExecutor(SessionFactory session, CRUDService crudService,
			AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory,
			DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory) {
		super();
		this.sessionFactory = session;
		this.crudService = crudService;
		this.authenticationBasedPropertiesFactory = authenticationBasedPropertiesFactory;
		this.departmentBasedPropertiesFactory = departmentBasedPropertiesFactory;
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public <E extends T> Optional<E> findOne(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query = resolvePredicate(root, query, builder, spec);

		return session.createQuery(query).getResultStream().findFirst();
	}

	@Override
	public <E extends T> List<E> findAll(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> query = builder.createQuery(type);
		Root<E> root = query.from(type);

		query = resolvePredicate(root, query, builder, spec);

		return session.createQuery(query).list();
	}

	@Override
	public <E extends T> Page<E> findAll(Class<E> type, Specification<E> spec, Pageable pageable) {
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
	public <E extends T> List<E> findAll(Class<E> type, Specification<E> spec, Sort sort) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<E> criteriaQuery = builder.createQuery(type);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);
		criteriaQuery = resolveSort(root, criteriaQuery, builder, sort);

		return session.createQuery(criteriaQuery).list();
	}

	@Override
	public <E extends T> long count(Class<E> type, Specification<E> spec) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery = resolvePredicate(root, criteriaQuery.select(builder.count(root)), builder, spec);

		return session.createQuery(criteriaQuery).getSingleResult();
	}

	@Override
	public <E extends T> Optional<Map<String, Object>> findOne(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException {
		List<String> validatedColumns = crudService.getDefaultColumns(type, role, requestedColumns);
		Tuple tuple = doFindOne(getCurrentSession(), type, validatedColumns, spec);

		if (tuple == null) {
			return EMPTY_PROPERTIES;
		}

		if (validatedColumns.size() == 1) {
			return Optional.of(authenticationBasedPropertiesFactory.singularProduce(type, tuple.get(0),
					validatedColumns.get(0), role));
		}

		return Optional.of(authenticationBasedPropertiesFactory.produce(type, tuple.toArray(),
				validatedColumns.toArray(String[]::new), role));
	}

	@Override
	public <E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException {
		List<String> validatedColumns = crudService.getDefaultColumns(type, role, requestedColumns);
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, validatedColumns, spec);

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		if (validatedColumns.size() == 1) {
			return authenticationBasedPropertiesFactory.singularProduce(type,
					tuples.stream().map(tuple -> tuple.get(0)).collect(Collectors.toList()), validatedColumns.get(0),
					role);
		}

		return authenticationBasedPropertiesFactory.produce(type,
				tuples.stream().map(tuple -> tuple.toArray()).collect(Collectors.toList()),
				validatedColumns.toArray(String[]::new), role);
	}

	@Override
	public <E extends T> Page<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Pageable pageable, Role role) throws NoSuchFieldException {
		List<String> validatedColumns = crudService.getDefaultColumns(type, role, requestedColumns);
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, validatedColumns, spec, pageable);

		if (tuples.isEmpty()) {
			return Page.empty();
		}

		if (validatedColumns.size() == 1) {
			return new PageImpl<>(authenticationBasedPropertiesFactory.singularProduce(type,
					tuples.stream().map(tuple -> tuple.get(0)).collect(Collectors.toList()), validatedColumns.get(0),
					role), pageable, tuples.size());
		}

		return new PageImpl<>(authenticationBasedPropertiesFactory.produce(type,
				tuples.stream().map(tuple -> tuple.toArray()).collect(Collectors.toList()),
				validatedColumns.toArray(String[]::new), role), pageable, tuples.size());
	}

	@Override
	public <E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Sort sort, Role role) throws NoSuchFieldException {
		List<String> validatedColumns = crudService.getDefaultColumns(type, role, requestedColumns);
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, validatedColumns, spec, sort);

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		if (validatedColumns.size() == 1) {
			return authenticationBasedPropertiesFactory.singularProduce(type,
					tuples.stream().map(tuple -> tuple.get(0)).collect(Collectors.toList()), validatedColumns.get(0),
					role);
		}

		return authenticationBasedPropertiesFactory.produce(type,
				tuples.stream().map(tuple -> tuple.toArray()).collect(Collectors.toList()),
				validatedColumns.toArray(String[]::new), role);
	}

	@SuppressWarnings("unchecked")
	private <E extends T> Class<? extends DepartmentScoped> assertDepartmentScopedType(Class<E> type) {
		Assert.isTrue(DepartmentScoped.class.isAssignableFrom(type), String.format("Unknown scoped type [%s]", type));

		return (Class<? extends DepartmentScoped>) type;
	}

	@Override
	public <E extends T> Optional<Map<String, Object>> findOne(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException {
		Class<? extends DepartmentScoped> scopedType = assertDepartmentScopedType(type);
		List<String> validatedColumns = crudService.getDefaultColumns(scopedType, departmentId, requestedColumns);
		Tuple tuple = doFindOne(getCurrentSession(), type, validatedColumns, spec);

		if (tuple == null) {
			return EMPTY_PROPERTIES;
		}

		if (validatedColumns.size() == 1) {
			return Optional.of(departmentBasedPropertiesFactory.singularProduce(scopedType, tuple.get(0),
					validatedColumns.get(0), departmentId));
		}

		return Optional.of(departmentBasedPropertiesFactory.produce(scopedType, tuple.toArray(),
				validatedColumns.toArray(String[]::new), departmentId));
	}

	@Override
	public <E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException {
		Class<? extends DepartmentScoped> scopedType = assertDepartmentScopedType(type);
		List<String> validatedColumns = crudService.getDefaultColumns(scopedType, departmentId, requestedColumns);
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, validatedColumns, spec);

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		if (validatedColumns.size() == 1) {
			return departmentBasedPropertiesFactory.singularProduce(scopedType,
					tuples.stream().map(tuple -> tuple.get(0)).collect(Collectors.toList()), validatedColumns.get(0),
					departmentId);
		}

		return departmentBasedPropertiesFactory.produce(scopedType,
				tuples.stream().map(tuple -> tuple.toArray()).collect(Collectors.toList()),
				validatedColumns.toArray(String[]::new), departmentId);
	}

	@Override
	public <E extends T> Page<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Pageable pageable, UUID departmentId) throws NoSuchFieldException {
		Class<? extends DepartmentScoped> scopedType = assertDepartmentScopedType(type);
		List<String> validatedColumns = crudService.getDefaultColumns(scopedType, departmentId, requestedColumns);
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, validatedColumns, spec, pageable);

		if (tuples.isEmpty()) {
			return Page.empty();
		}

		if (validatedColumns.size() == 1) {
			return new PageImpl<>(departmentBasedPropertiesFactory.singularProduce(scopedType,
					tuples.stream().map(tuple -> tuple.get(0)).collect(Collectors.toList()), validatedColumns.get(0),
					departmentId), pageable, tuples.size());
		}

		return new PageImpl<>(departmentBasedPropertiesFactory.produce(scopedType,
				tuples.stream().map(tuple -> tuple.toArray()).collect(Collectors.toList()),
				validatedColumns.toArray(String[]::new), departmentId), pageable, tuples.size());
	}

	@Override
	public <E extends T> List<Map<String, Object>> findAll(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Sort sort, UUID departmentId) throws NoSuchFieldException {
		Class<? extends DepartmentScoped> scopedType = assertDepartmentScopedType(type);
		List<String> validatedColumns = crudService.getDefaultColumns(scopedType, departmentId, requestedColumns);
		List<Tuple> tuples = doFindAll(getCurrentSession(), type, validatedColumns, spec, sort);

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		if (validatedColumns.size() == 1) {
			return departmentBasedPropertiesFactory.singularProduce(scopedType,
					tuples.stream().map(tuple -> tuple.get(0)).collect(Collectors.toList()), validatedColumns.get(0),
					departmentId);
		}

		return departmentBasedPropertiesFactory.produce(scopedType,
				tuples.stream().map(tuple -> tuple.toArray()).collect(Collectors.toList()),
				validatedColumns.toArray(String[]::new), departmentId);
	}

	private <E> Tuple doFindOne(Session session, Class<E> type, Collection<String> validatedColumns,
			Specification<E> spec) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
		Root<E> root = query.from(type);

		query.multiselect(validatedColumns.stream().map(column -> root.get(column)).collect(Collectors.toList()));
		query = resolvePredicate(root, query, builder, spec);

		return session.createQuery(query).getResultStream().findFirst().orElse(null);
	}

	private <E> List<Tuple> doFindAll(Session session, Class<E> type, Collection<String> validatedColumns,
			Specification<E> spec) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery
				.multiselect(validatedColumns.stream().map(column -> root.get(column)).collect(Collectors.toList()));
		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);

		return session.createQuery(criteriaQuery).list();
	}

	private <E extends T> List<Tuple> doFindAll(Session session, Class<E> type, Collection<String> validatedColumns,
			Specification<E> spec, Pageable pageable) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery
				.multiselect(validatedColumns.stream().map(column -> root.get(column)).collect(Collectors.toList()));
		criteriaQuery = resolvePredicate(root, criteriaQuery, builder, spec);

		return resolvePagedQuery(session, criteriaQuery, pageable).list();
	}

	private <E extends T> List<Tuple> doFindAll(Session session, Class<E> type, Collection<String> validatedColumns,
			Specification<E> spec, Sort sort) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<E> root = criteriaQuery.from(type);

		criteriaQuery
				.multiselect(validatedColumns.stream().map(column -> root.get(column)).collect(Collectors.toList()));
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
			throw new IllegalArgumentException(EmptyPredicateException.INSTANCE);
		}

		return criteriaQuery.where(predicate);
	}

}
