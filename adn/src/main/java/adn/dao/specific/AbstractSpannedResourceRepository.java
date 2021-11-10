/**
 * 
 */
package adn.dao.specific;

import static adn.application.context.ContextProvider.getCurrentSession;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;

import org.hibernate.Session;
import org.hibernate.metamodel.model.domain.internal.AbstractAttribute;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import adn.dao.generic.GenericRepository;
import adn.dao.generic.GenericRepositoryImpl;
import adn.dao.generic.SpannedResourceRepository;
import adn.helpers.CollectionHelper;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractSpannedResourceRepository<T extends Temporal, E extends Entity>
		implements SpannedResourceRepository<T, E> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSpannedResourceRepository.class);

	private static final String CURRENT_TEMPORAL_PARAM_NAME = "now";
	private static final String PATH_DELIMITER = ".";

	private final Class<E> entityType;
	private final GenericRepository genericRepository;
	private final Function<Root<E>, Path<Comparable>> appliedTimestampPathResolver;
	private final Function<Root<E>, Path<Comparable>> droppedTimestampPathResolver;
	private final Supplier<Comparable> currentTemporalSupplier;
	private final String findAllCurrentsQueryTemplate;

	// @formatter:off
	public AbstractSpannedResourceRepository(
			Class<E> entityType,
			GenericRepository genericRepository,
			Function<Root<E>, Path<Comparable>> appliedTimestampPathResolver,
			Function<Root<E>, Path<Comparable>> droppedTimestampPathResolver,
			Supplier<Comparable> currentTemporalSupplier) {
		super();
		this.entityType = entityType;
		this.genericRepository = genericRepository;
		this.appliedTimestampPathResolver = appliedTimestampPathResolver;
		this.droppedTimestampPathResolver = droppedTimestampPathResolver;
		this.currentTemporalSupplier = currentTemporalSupplier;
		
		CriteriaBuilder builder = HibernateHelper.getSessionFactory().getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<E> root = cq.from(entityType);
		// @formatter:off
		findAllCurrentsQueryTemplate = String.format(
				"SELECT %s FROM %s WHERE %s AND %s BETWEEN %s AND %s",
				"%s",
				HibernateHelper.getEntityName(entityType),
				"%s",
				':' + CURRENT_TEMPORAL_PARAM_NAME,
				resolvePathAsString(appliedTimestampPathResolver.apply(root)), 
				resolvePathAsString(droppedTimestampPathResolver.apply(root)));
		// @formatter:on
	}

	private String resolvePathAsString(Path<?> leadingNode) {
		Path pathNode = leadingNode;
		LinkedList<String> pathStack = new LinkedList<String>();
		AbstractAttribute<?, ?> bindable = (AbstractAttribute<?, ?>) pathNode.getModel();

		pathStack.push(bindable.getName());

		while ((pathNode = pathNode.getParentPath()) != null) {
			if (!(pathNode.getModel() instanceof Attribute)) {
				break;
			}

			bindable = (AbstractAttribute<?, ?>) pathNode.getModel();
			pathStack.push(bindable.getName());
		}

		return StringHelper.join(PATH_DELIMITER, pathStack.toArray());
	}

	// @formatter:on
	@Override
	public Optional<E> findCurrent(Specification<E> specification) {
		return findCurrent(specification, getCurrentSession());
	}

	@Override
	public Optional<E> findCurrent(Specification<E> specification, Session session) {
		return genericRepository.findOne(entityType, specification.and(getCurrentSpecification()), session);
	}

	@Override
	public Optional<Object[]> findCurrent(Collection<String> columns, Specification<E> specification) {
		return findCurrent(columns, specification, getCurrentSession());
	}

	@Override
	public Optional<Object[]> findCurrent(Collection<String> columns, Specification<E> specification, Session session) {
		return genericRepository.findOne(entityType, columns, specification.and(getCurrentSpecification()), session);
	}

	@Override
	public List<E> findAllCurrents(Specification<E> specification) {
		return findAllCurrents(specification, getCurrentSession());
	}

	@Override
	public List<E> findAllCurrents(Specification<E> specification, Session session) {
		return genericRepository.findAll(entityType, specification.and(getCurrentSpecification()), session);
	}

	@Override
	public List<Object[]> findAllCurrents(Collection<String> columns, Specification<E> specification) {
		return findAllCurrents(columns, specification, getCurrentSession());
	}

	@Override
	public List<Object[]> findAllCurrents(Collection<String> columns, Specification<E> specification, Session session) {
		return genericRepository.findAll(entityType, columns, specification.and(getCurrentSpecification()), session);
	}

	@Override
	public List<E> findAllCurrents(String specification, Object... parameterValues) {
		return findAllCurrents(specification, getCurrentSession(), parameterValues);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> findAllCurrents(String specification, Session session, Object... parameterValues) {
		return (List<E>) doFindAllCurrents(null, specification, session, parameterValues);
	}

	@Override
	public List<Object[]> findAllCurrents(Collection<String> columns, String specification, Object... parameterValues) {
		return findAllCurrents(columns, specification, getCurrentSession(), parameterValues);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findAllCurrents(Collection<String> columns, String specification, Session session,
			Object... parameterValues) {
		return HibernateHelper
				.toRows((List<Tuple>) doFindAllCurrents(columns, specification, session, parameterValues));
	}

	private List<?> doFindAllCurrents(Collection<String> columns, String specification, Session session,
			Object... parameterValues) {
		Query<?> query = resolveQuery(session, columns, specification);

		query.setParameter(CURRENT_TEMPORAL_PARAM_NAME, currentTemporalSupplier.get());
		IntStream.range(0, parameterValues.length)
				.forEach(index -> query.setParameter(index + 1, parameterValues[index]));

		if (logger.isDebugEnabled()) {
			logger.debug(query.getQueryString());
		}

		return query.getResultList();
	}

	private Query<?> resolveQuery(Session session, Collection<String> columns, String specification) {
		String selections = CollectionHelper.isEmpty(columns) ? GenericRepositoryImpl.SELECT_ALL_CHARACTER
				: StringHelper.join(columns.toArray());
		String hql = String.format(findAllCurrentsQueryTemplate, selections, specification);

		return selections.equals(GenericRepositoryImpl.SELECT_ALL_CHARACTER) ? session.createQuery(hql)
				: session.createQuery(hql, Tuple.class);
	}

	@Override
	public Optional<E> findOverlapping(Specification<E> specification, T appliedTimestamp, T droppedTimestamp) {
		return findOverlapping(specification, appliedTimestamp, droppedTimestamp, getCurrentSession());
	}

	@Override
	public Optional<E> findOverlapping(Specification<E> specification, T appliedTimestamp, T droppedTimestamp,
			Session session) {
		return genericRepository.findOne(entityType,
				specification.and(getSpecificationForOverlapping(appliedTimestamp, droppedTimestamp)), session);
	}

	@Override
	public Optional<Object[]> findOverlapping(Collection<String> columns, Specification<E> specification,
			T appliedTimestamp, T droppedTimestamp) {
		return findOverlapping(columns, specification, appliedTimestamp, droppedTimestamp, getCurrentSession());
	}

	@Override
	public Optional<Object[]> findOverlapping(Collection<String> columns, Specification<E> specification,
			T appliedTimestamp, T droppedTimestamp, Session session) {
		return genericRepository.findOne(entityType, columns,
				specification.and(getSpecificationForOverlapping(appliedTimestamp, droppedTimestamp)), session);
	}

	@SuppressWarnings("unchecked")
	protected Specification<E> getCurrentSpecification() {
		Comparable now = currentTemporalSupplier.get();
		// @formatter:off
		return (root, query, builder) -> builder.between(builder.literal(now), 
				appliedTimestampPathResolver.apply(root),
				droppedTimestampPathResolver.apply(root));
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private Specification<E> getSpecificationForOverlapping(T appliedTimestamp, T droppedTimestamp) {
		// @formatter:off
		return (root, query, builder) -> builder.and(
				builder.greaterThanOrEqualTo(droppedTimestampPathResolver.apply(root), (Comparable) appliedTimestamp),
				builder.lessThanOrEqualTo(appliedTimestampPathResolver.apply(root), (Comparable) droppedTimestamp));
		// @formatter:on
	}

	static abstract class AbstractLocalDateTimeSpannedResourceRepository<E extends Entity>
			extends AbstractSpannedResourceRepository<LocalDateTime, E> {

		public AbstractLocalDateTimeSpannedResourceRepository(Class<E> entityType, GenericRepository genericRepository,
				Function<Root<E>, Path<Comparable>> appliedTimestampPathResolver,
				Function<Root<E>, Path<Comparable>> droppedTimestampPathResolver) {
			super(entityType, genericRepository, appliedTimestampPathResolver, droppedTimestampPathResolver,
					() -> LocalDateTime.now());
		}

	}

}
