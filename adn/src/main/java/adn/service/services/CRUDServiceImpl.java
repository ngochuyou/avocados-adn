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
import static adn.helpers.ArrayHelper.from;
import static adn.helpers.EntityUtils.getEntityName;
import static adn.helpers.EntityUtils.getIdentifier;
import static adn.helpers.EntityUtils.getIdentifierPropertyName;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.application.context.ContextProvider;
import adn.dao.generic.AbstractRepository;
import adn.dao.generic.Result;
import adn.dao.generic.ResultBatch;
import adn.helpers.EntityUtils;
import adn.model.DepartmentScoped;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.entity.builder.EntityBuilder;
import adn.service.entity.builder.EntityBuilderProvider;
import adn.service.internal.CRUDService;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Primary
public final class CRUDServiceImpl implements CRUDService {

	private static final Logger logger = LoggerFactory.getLogger(CRUDServiceImpl.class);

	private final ModelContextProvider modelContext;
	private final AbstractRepository repository;
	private final EntityBuilderProvider entityBuilderProvider;

	private final AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory;
	private final DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory;

	private static final String GENERIC_ALIAS = "e";
	public static final String EXECUTOR_NAME = "CRUDServiceBatchExecutor";
	private static final int MAXIMUM_BATCHSIZE_IN_SINGULAR_PROCESS = 100;
	private static final int MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS = 50;
	private static final int MAXIMUM_BATCH_SIZE = 1000;
	protected static final String INVALID_CONSTRAINT = "Invalid constraint";
	protected static final Map<String, String> INVALID_CONSTRAINT_MESSAGE_SET = Map.of("constraint",
			INVALID_CONSTRAINT);

	@Autowired
	private BatchWorker batchWorker;

	// @formatter:off
	@Autowired
	public CRUDServiceImpl(
			AbstractRepository baseRepository,
			EntityBuilderProvider entityBuilderProvider,
			AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory,
			AuthenticationBasedModelFactory authenticationBasedModelFactory,
			DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory,
			ModelContextProvider modelContext) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
		this.authenticationBasedPropertiesFactory = authenticationBasedModelPropertiesFactory;
		this.departmentBasedPropertiesFactory = departmentBasedPropertiesFactory;
		this.modelContext = modelContext;
	}

	public CRUDServiceImpl() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		
		this.repository = context.getBean(AbstractRepository.class);
		this.entityBuilderProvider = context.getBean(EntityBuilderProvider.class);
		this.authenticationBasedPropertiesFactory = context.getBean(AuthenticationBasedModelPropertiesFactory.class);
		this.departmentBasedPropertiesFactory = context.getBean(DepartmentBasedModelPropertiesFactory.class);
		this.modelContext = context.getBean(ModelContextProvider.class);
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

		return resolveReadResult(type, rows, validatedColumns, role);
	}

	private String prependAlias(String columnName) {
		return GENERIC_ALIAS + "." + columnName;
	}

	/**
	 * We don't validate associating attribute name, that's up to devs as a contract
	 */
	@Override
	public <T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, Serializable associationIdentifier,
			Collection<String> columns, Pageable pageable, Role role) throws NoSuchFieldException {
		Collection<String> validatedColumns = getDefaultColumns(type, role, columns);
		List<?> rows = repository.find(
				String.format("""
						SELECT %s FROM %s e WHERE e.%s.%s=:associationIdentifier
						""", validatedColumns.stream().map(this::prependAlias).collect(Collectors.joining(",")),
						getEntityName(type), associatingAttribute, getIdentifierPropertyName(associatingType)),
				Map.of("associationIdentifier", associationIdentifier));

		return resolveReadResult(type, rows, from(validatedColumns), role);
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
	public <T extends DepartmentScoped> List<String> getDefaultColumns(Class<T> type, UUID departmentId,
			Collection<String> columns) throws NoSuchFieldException {
		if (!Entity.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException(String.format("Unknown entity type [%s]", type.getName()));
		}
			
		if (columns.isEmpty()) {
			DomainEntityMetadata metadata = modelContext.getMetadata((Class<Entity>) type);

			return new ArrayList<>(metadata.getNonLazyPropertyNames());
		}

		return new ArrayList<>(
				departmentBasedPropertiesFactory.validateColumnNames(type, columns));
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

	protected <T extends Entity> Serializable resolveId(Serializable id, T entity) {
		return id == null ? getIdentifier(entity) : id;
	}

	protected <T extends Entity> List<Serializable> resolveIds(Collection<T> pairs) {
		return pairs.stream().map(EntityUtils::getIdentifier).collect(Collectors.toList());
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

	@SuppressWarnings("unchecked")
	protected <T extends Entity> List<Map<String, Object>> resolveReadResult(Class<T> type, List<?> source,
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

	@Component
	public class BatchWorker {

		@Async(CRUDServiceImpl.EXECUTOR_NAME)
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

}
