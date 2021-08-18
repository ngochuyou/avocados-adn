/**
 * 
 */
package adn.service.services;

import static adn.application.context.ContextProvider.getPrincipalRole;
import static adn.dao.generic.Result.bad;
import static adn.dao.generic.Result.failed;
import static adn.dao.generic.ResultBatch.bad;
import static adn.dao.generic.ResultBatch.failed;
import static adn.dao.generic.ResultBatch.ok;
import static adn.helpers.CollectionHelper.from;
import static adn.helpers.HibernateHelper.getEntityName;
import static adn.helpers.HibernateHelper.getIdentifier;
import static adn.helpers.HibernateHelper.getIdentifierPropertyName;
import static adn.helpers.HibernateHelper.toRows;
import static adn.service.internal.Service.Status.BAD;
import static adn.service.internal.Service.Status.FAILED;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Tuple;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.FlushMode;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.application.context.ContextProvider;
import adn.application.context.builders.EntityBuilderProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.dao.generic.GenericRepository;
import adn.dao.generic.Result;
import adn.dao.generic.ResultBatch;
import adn.dao.specification.Selections;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.DepartmentScoped;
import adn.model.entities.Entity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.entity.builder.EntityBuilder;
import adn.service.internal.CRUDService;
import adn.service.internal.Role;
import adn.service.specification.GenericJpaSpecificationExecutor;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Primary
public final class GenericCRUDService implements CRUDService {

	private static final Logger logger = LoggerFactory.getLogger(GenericCRUDService.class);

	private final ModelContextProvider modelContext;
	private final EntityBuilderProvider entityBuilderProvider;

	private final GenericRepository repository;
	private final GenericJpaSpecificationExecutor genericSpecificationExecutor;
	private final AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory;
	private final DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory;

	public static final String EXECUTOR_NAME = "CRUDServiceBatchExecutor";
	private static final int MAXIMUM_BATCHSIZE_IN_SINGULAR_PROCESS = 100;
	private static final int MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS = 50;
	private static final int MAXIMUM_BATCH_SIZE = 1000;
	private static final String INVALID_CONSTRAINT = "Invalid constraint";
	private static final Map<String, String> INVALID_CONSTRAINT_MESSAGE_SET = Map.of("constraint", INVALID_CONSTRAINT);

	@Autowired
	private BatchWorker batchWorker;

	// @formatter:off
	@Autowired
	public GenericCRUDService(
			GenericRepository baseRepository,
			EntityBuilderProvider entityBuilderProvider,
			AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory,
			AuthenticationBasedModelFactory authenticationBasedModelFactory,
			DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory,
			ModelContextProvider modelContext,
			GenericJpaSpecificationExecutor genericSpecificationExecutor) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
		this.authenticationBasedPropertiesFactory = authenticationBasedModelPropertiesFactory;
		this.departmentBasedPropertiesFactory = departmentBasedPropertiesFactory;
		this.modelContext = modelContext;
		this.genericSpecificationExecutor = genericSpecificationExecutor;
	}

	public GenericCRUDService() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		
		this.repository = context.getBean(GenericRepository.class);
		this.entityBuilderProvider = context.getBean(EntityBuilderProvider.class);
		this.authenticationBasedPropertiesFactory = context.getBean(AuthenticationBasedModelPropertiesFactory.class);
		this.departmentBasedPropertiesFactory = context.getBean(DepartmentBasedModelPropertiesFactory.class);
		this.modelContext = context.getBean(ModelContextProvider.class);
		this.genericSpecificationExecutor = context.getBean(GenericJpaSpecificationExecutor.class);
	}
	// @formatter:on
	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns,
			Pageable pageable) throws NoSuchFieldException {
		return read(type, columns, pageable, getPrincipalRole());
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns,
			Pageable pageable, Role role) throws NoSuchFieldException {
		String[] validatedColumns = from(getDefaultColumns(type, role, columns));
		List<Object[]> rows = repository.fetch(type, validatedColumns, pageable);

		if (rows.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}

		if (rows.get(0).length == 0) {
			return new ArrayList<Map<String, Object>>();
		}

		return resolveReadResults(type, rows, validatedColumns, role);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns,
			Pageable pageable, UUID departmentId) throws NoSuchFieldException {
		String[] validatedColumns = from(getDefaultColumns(type, departmentId, columns));
		List<Object[]> rows = repository.fetch(type, validatedColumns, pageable);

		if (rows.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}

		if (rows.get(0).length == 0) {
			return new ArrayList<Map<String, Object>>();
		}

		return resolveReadResults(type, rows, validatedColumns, departmentId);
	}

	private String prependAlias(String columnName) {
		return "e." + columnName;
	}

	/**
	 * We don't validate associating attribute name, that's up to devs as a contract
	 */
	@Override
	public <T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Role role)
			throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, role, columns);

		try {
			List<?> rows = repository.find(String.format("""
					SELECT %s FROM %s e WHERE e.%s.%s=:associationIdentifier
					""", validatedColumns.stream().map(this::prependAlias).collect(Collectors.joining(",")),
					getEntityName(type), associatingAttribute,
					!StringHelper.hasLength(associationProperty) ? getIdentifierPropertyName(associatingType)
							: associationProperty),
					Map.of("associationIdentifier", associationIdentifier));

			return resolveReadResults(type, rows, from(validatedColumns), role);
		} catch (Exception any) {
			// the association property is not checked so QueryException
			// will be thrown when the association property column does not exist
			if (any.getCause() instanceof QueryException) {
				throw new NoSuchFieldException(String.format("Unknown columns %s", associationProperty));
			}

			throw any;
		}
	}

	@Override
	public <T extends Entity> List<String> getDefaultColumns(Class<T> type, Role role, Collection<String> columns)
			throws NoSuchFieldException {
		if (columns.isEmpty()) {
			DomainEntityMetadata metadata = modelContext.getMetadata(type);

			return new ArrayList<>(metadata.getNonLazyPropertyNames());
		}

		return new ArrayList<>(
				authenticationBasedPropertiesFactory.validateAndTranslateColumnNames(type, role, columns));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> List<String> getDefaultColumns(Class<T> type, UUID departmentId,
			Collection<String> columns) throws NoSuchFieldException {
		if (!DepartmentScoped.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException(String.format("Type [%s] is not of type %a", type.getName(),
					DepartmentScoped.class.getSimpleName()));
		}

		if (columns.isEmpty()) {
			DomainEntityMetadata metadata = modelContext.getMetadata(type);

			return new ArrayList<>(metadata.getNonLazyPropertyNames());
		}

		return new ArrayList<>(
				departmentBasedPropertiesFactory.validateColumnNames((Class<DepartmentScoped>) type, columns));
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns)
			throws NoSuchFieldException {
		return find(id, type, columns, getPrincipalRole());
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			Role role) throws NoSuchFieldException {
		String[] validatedColumns = from(getDefaultColumns(type, role, columns));
		Object[] row = repository.findById(id, type, validatedColumns);

		if (row == null) {
			return null;
		}

		return authenticationBasedPropertiesFactory.produce(type, row, validatedColumns, role);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			UUID departmentId) throws NoSuchFieldException {
		String[] validatedColumns = from(getDefaultColumns(type, departmentId, columns));
		Object[] row = repository.findById(id, type, validatedColumns);

		if (row == null) {
			return null;
		}

		return departmentBasedPropertiesFactory.produce((Class<? extends DepartmentScoped>) type, row, validatedColumns,
				departmentId);
	}

	protected <T extends Entity> Serializable resolveId(Serializable id, T entity) {
		return id == null ? getIdentifier(entity) : id;
	}

	protected <T extends Entity> List<Serializable> resolveIds(Collection<T> pairs) {
		return pairs.stream().map(HibernateHelper::getIdentifier).collect(Collectors.toList());
	}

	@Override
	public <T extends Entity, E extends T> Result<E> create(Serializable id, E entity, Class<E> type,
			boolean flushOnFinish) {
		id = resolveId(id, entity);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for creation [%s#%s]", type.getName(), id));
		}

		entity = entityBuilder.buildInsertion(id, entity);

		return finish(ss, repository.insert(id, entity, type), flushOnFinish);
	}

	@Override
	public <T extends Entity, E extends T> ResultBatch<E> createBatch(Collection<E> batch, Class<E> type,
			boolean flushOnFinish) {
		List<E> entityBatch = batch instanceof List ? (List<E>) batch : new ArrayList<>(batch);
		List<Serializable> ids = resolveIds(batch);
		Session session = getCurrentSession();

		session.setHibernateFlushMode(FlushMode.MANUAL);

		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (batch.size() < MAXIMUM_BATCHSIZE_IN_SINGULAR_PROCESS) {
			return singularlyCreateBatch(session, type, entityBuilder, ids, entityBatch, flushOnFinish);
		}

		return parallellyCreateBatch(session, type, entityBuilder, ids, entityBatch, flushOnFinish);
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, E extends T> ResultBatch<E> parallellyCreateBatch(Session session, Class<E> type,
			EntityBuilder<E> builder, List<Serializable> idBatch, List<E> entityBatch, boolean flushOnFinish) {
		int size = entityBatch.size();

		if (size > MAXIMUM_BATCH_SIZE) {
			return ResultBatch.<E>bad(Collections.emptyList())
					.setMessage(String.format("Maximum elements in one batch is %d", MAXIMUM_BATCH_SIZE));
		}

		int biggestFullBatchAmount = size / MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS;
		int amountInLastBatch = size % MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS;
		EnumSet<Status> statusSet = EnumSet.noneOf(Status.class);
		Result<E>[] resultSet = (Result<E>[]) Array.newInstance(Result.class, size);
		CountDownLatch latch = new CountDownLatch(
				amountInLastBatch == 0 ? biggestFullBatchAmount : biggestFullBatchAmount + 1);

		IntStream.range(0, biggestFullBatchAmount).forEach(i -> {
			int startIndex = i * MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS;
			int endIndex = startIndex + MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS;

			batchWorker.executeBatchCreate(session, idBatch, entityBatch, statusSet, resultSet, type, builder,
					startIndex, endIndex, latch);
		});

		if (amountInLastBatch != 0) {
			int startIndex = biggestFullBatchAmount * MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS;
			int endIndex = startIndex + amountInLastBatch;

			batchWorker.executeBatchCreate(session, idBatch, entityBatch, statusSet, resultSet, type, builder,
					startIndex, endIndex, latch);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return finish(session, null, EnumSet.of(FAILED), false);
		}

		return finish(session, Arrays.asList(resultSet), statusSet, flushOnFinish);
	}

	private <T extends Entity, E extends T> ResultBatch<E> singularlyCreateBatch(Session session, Class<E> type,
			EntityBuilder<E> builder, List<Serializable> idBatch, List<E> entityBatch, boolean flushOnFinish) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Executing singularly creation on a batch with entities of [%s] using [%s]",
					type.getName(), builder.getClass().getName()));
		}

		final EnumSet<Status> statusBatch = EnumSet.noneOf(Status.class);

		return finish(session, IntStream.range(0, entityBatch.size()).mapToObj(index -> {
			Serializable id = idBatch.get(index);
			Result<E> result = repository.insert(id, builder.buildInsertion(id, entityBatch.get(index)), type);

			statusBatch.add(result.getStatus());

			return result;
		}).collect(Collectors.toList()), statusBatch, flushOnFinish);
	}

	protected <E> Result<E> finish(Session ss, Result<E> result, boolean flushOnFinish) {
		if (flushOnFinish) {
			try {
				if (result.isOk()) {
					ss.flush();

					return result;
				}

				ss.clear();

				return result;
			} catch (Exception any) {
				Throwable cause = any.getCause();

				if (cause instanceof ConstraintViolationException) {
					return bad(INVALID_CONSTRAINT_MESSAGE_SET);
				}

				any.printStackTrace();
				return failed(any.getMessage());
			}
		}

		return result;
	}

	protected <E> ResultBatch<E> finish(Session ss, List<Result<E>> resultBatch, EnumSet<Status> statusBatch,
			boolean flushOnFinish) {
		boolean hasFailed;
		boolean isOk = !(hasFailed = statusBatch.contains(FAILED)) && !statusBatch.contains(BAD);

		if (flushOnFinish) {
			try {
				if (isOk) {
					ss.flush();

					return ok(resultBatch);
				}

				ss.clear();

				return hasFailed ? failed(resultBatch) : bad(resultBatch);
			} catch (Exception any) {
				Throwable cause = any.getCause();

				if (cause instanceof ConstraintViolationException) {
					return ResultBatch.<E>bad(Arrays.asList(Result.bad(INVALID_CONSTRAINT_MESSAGE_SET)));
				}

				any.printStackTrace();
				return ResultBatch.<E>failed(null).setMessage(any.getMessage());
			}
		}

		return isOk ? ok(resultBatch) : hasFailed ? failed(resultBatch) : bad(resultBatch);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Serializable id, E entity, Class<E> type,
			boolean flushOnFinish) {
		id = resolveId(id, entity);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		E persistence = ss.load(type, id);
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for update [%s#%s]", type.getName(), id));
		}
		// persistence takes effects during updateBuild,
		// assigning it to the return of updateBuild is just for the sake of it
		entityBuilder.buildUpdate(id, entity, persistence);

		return finish(ss, repository.update(id, (E) ss.load(type, id), type), flushOnFinish);
	}

	protected Map.Entry<Integer, Long> resolveLimitOffset(Pageable paging) {
		return Map.entry(paging.getPageSize(), Long.valueOf(paging.getPageNumber() * paging.getPageSize()));
	}

	protected <T extends Entity> Map<String, Object> resolveReadResult(Class<T> type, Object source,
			String[] validatedColumns, Role role) {
		if (source == null) {
			return null;
		}

		if (source.getClass().isArray()) {
			return authenticationBasedPropertiesFactory.produce(type, (Object[]) source, validatedColumns, role);
		}

		return authenticationBasedPropertiesFactory.singularProduce(type, source, validatedColumns[0], role);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity> List<Map<String, Object>> resolveReadResults(Class<T> type, List<?> source,
			String[] validatedColumns, Role role) {
		if (source.isEmpty()) {
			return new ArrayList<>();
		}

		if (source.get(0).getClass().isArray()) {
			return authenticationBasedPropertiesFactory.produce(type, (List<Object[]>) source, validatedColumns, role);
		}

		return authenticationBasedPropertiesFactory.singularProduce(type, (List<Object>) source, validatedColumns[0],
				role);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity> Map<String, Object> resolveReadResult(Class<T> type, Object source,
			String[] validatedColumns, UUID departmentId) {
		if (source == null) {
			return null;
		}

		if (source.getClass().isArray()) {
			return departmentBasedPropertiesFactory.produce((Class<DepartmentScoped>) type, (Object[]) source,
					validatedColumns, departmentId);
		}

		return departmentBasedPropertiesFactory.singularProduce((Class<DepartmentScoped>) type, source,
				validatedColumns[0], departmentId);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity> List<Map<String, Object>> resolveReadResults(Class<T> type, List<?> source,
			String[] validatedColumns, UUID departmentId) {
		if (source.isEmpty()) {
			return new ArrayList<>();
		}

		if (source.get(0).getClass().isArray()) {
			return departmentBasedPropertiesFactory.produce((Class<DepartmentScoped>) type, (List<Object[]>) source,
					validatedColumns, departmentId);
		}

		return departmentBasedPropertiesFactory.singularProduce((Class<DepartmentScoped>) type, (List<Object>) source,
				validatedColumns[0], departmentId);
	}

	@Component
	public class BatchWorker {

		@Async(GenericCRUDService.EXECUTOR_NAME)
		@Transactional
		<T extends Entity> CompletableFuture<Void> executeBatchCreate(
		// @formatter:off
			final Session session,
			final List<Serializable> idBatch,
			final List<T> entityBatch,
			final EnumSet<Status> statusSet,
			final Result<T>[] results,
			final Class<T> type,
			final EntityBuilder<T> builder,
			final int start,
			final int end,
			final CountDownLatch latch
		// @formatter:on
		) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Executing creation batch in [%s]", Thread.currentThread().getName()));
			}

			IntStream.range(start, end).forEach(index -> {
				try {
					Serializable id = idBatch.get(index);
					Result<T> result = repository.insert(session, id,
							builder.buildInsertion(id, entityBatch.get(index)), type);

					results[index] = result;
					statusSet.add(result.getStatus());
				} catch (Exception any) {
					any.printStackTrace();
					results[index] = Result.failed(any.getMessage());
					statusSet.add(Status.FAILED);
				}
			});

			return new CompletableFuture<>();
		}

	}

	private <T extends Entity, E extends T> Selections<E> getSelections(Collection<String> validatedColumns) {
		return new Selections<E>() {
			@Override
			public List<Selection<?>> toSelections(Root<E> root) {
				return validatedColumns.stream().map(column -> root.get(column)).collect(Collectors.toList());
			}
		};
	}

	@Override
	public <T extends Entity, E extends T> Map<String, Object> find(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, Role role) throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, role, requestedColumns);
		Optional<Tuple> optionalRow = genericSpecificationExecutor.findOne(type, getSelections(validatedColumns), spec);
		Tuple row = optionalRow.get();

		if (row == null) {
			return null;
		}

		return resolveReadResult(type, row.toArray(), from(validatedColumns), role);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, Role role) throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, role, requestedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), role);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, Pageable pageable, Role role)
			throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, role, requestedColumns);
		Page<Tuple> page = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec, pageable);

		return resolveReadResults(type, toRows(page.getContent()), from(validatedColumns), role);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, Sort sort, Role role)
			throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, role, requestedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec, sort);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), role);
	}

	@Override
	public <T extends Entity, E extends T> Map<String, Object> find(Class<E> type, Collection<String> requestedColumns,
			Specification<E> spec, UUID departmentId) throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, departmentId, requestedColumns);
		Optional<Tuple> optionalRow = genericSpecificationExecutor.findOne(type, getSelections(validatedColumns), spec);
		Tuple row = optionalRow.get();

		if (row == null) {
			return null;
		}

		return resolveReadResult(type, row.toArray(), from(validatedColumns), departmentId);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, UUID departmentId) throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, departmentId, requestedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), departmentId);
	}

	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, UUID departmentId,
			Function<Collection<String>, Selections<E>> selectionResolver) throws NoSuchFieldException {
		if (selectionResolver == null) {
			return read(type, requestedColumns, spec, departmentId);
		}

		Collection<String> validatedColumns = getDefaultColumns(type, departmentId, requestedColumns);
		Selections<E> selections = selectionResolver.apply(validatedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, selections, spec);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), departmentId);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, Pageable pageable, UUID departmentId)
			throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, departmentId, requestedColumns);
		Page<Tuple> page = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec, pageable);

		return resolveReadResults(type, toRows(page.getContent()), from(validatedColumns), departmentId);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type,
			Collection<String> requestedColumns, Specification<E> spec, Sort sort, UUID departmentId)
			throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, departmentId, requestedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec, sort);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), departmentId);
	}

}
