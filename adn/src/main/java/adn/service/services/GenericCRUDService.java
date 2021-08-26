/**
 * 
 */
package adn.service.services;

import static adn.dao.generic.Result.bad;
import static adn.dao.generic.Result.failed;
import static adn.dao.generic.ResultBatch.bad;
import static adn.dao.generic.ResultBatch.failed;
import static adn.dao.generic.ResultBatch.ok;
import static adn.helpers.CollectionHelper.from;
import static adn.helpers.CollectionHelper.list;
import static adn.helpers.HibernateHelper.getEntityName;
import static adn.helpers.HibernateHelper.getIdentifier;
import static adn.helpers.HibernateHelper.getIdentifierPropertyName;
import static adn.helpers.HibernateHelper.toRows;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArray;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArrayCollection;
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
import adn.model.entities.Entity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.entity.builder.EntityBuilder;
import adn.service.internal.CRUDService;
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

	protected final GenericRepository repository;
	private final GenericJpaSpecificationExecutor genericSpecificationExecutor;
	protected final DynamicMapModelProducerFactory dynamicMapModelFactory;

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
			ModelContextProvider modelContext,
			GenericJpaSpecificationExecutor genericSpecificationExecutor,
			DynamicMapModelProducerFactory dynamicMapModelFactory) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
		this.modelContext = modelContext;
		this.genericSpecificationExecutor = genericSpecificationExecutor;
		this.dynamicMapModelFactory = dynamicMapModelFactory;
	}

	public GenericCRUDService() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		
		this.repository = context.getBean(GenericRepository.class);
		this.entityBuilderProvider = context.getBean(EntityBuilderProvider.class);
		this.modelContext = context.getBean(ModelContextProvider.class);
		this.genericSpecificationExecutor = context.getBean(GenericJpaSpecificationExecutor.class);
		this.dynamicMapModelFactory = context.getBean(DynamicMapModelProducerFactory.class);
	}
	// @formatter:on
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

	/*
	 * ==================================MIGRATION==================================
	 */

	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return find(id, type, columns, credential, unknownArray(type, list(columns)));
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		String[] validatedColumns = from(getDefaultColumns(type, credential, columns));
		Object[] row = repository.findById(id, type, validatedColumns);

		if (row == null) {
			return null;
		}

		return dynamicMapModelFactory.produce(row, sourceMetadata, credential);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity> List<Map<String, Object>> resolveReadResults(Class<T> type, List<?> source,
			String[] validatedColumns, Credential credential, SourceMetadata<T> sourceMetadata)
			throws UnauthorizedCredential {
		if (source.get(0).getClass().isArray()) {
			return dynamicMapModelFactory.produce((List<Object[]>) source, null, credential);
		}

		return dynamicMapModelFactory.produceSingular((List<Object>) source, sourceMetadata, credential);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity> List<Map<String, Object>> resolveReadResults(Class<T> type, List<?> source,
			Collection<String> validatedColumns, Credential credential) throws UnauthorizedCredential {
		if (source.get(0).getClass().isArray()) {
			return dynamicMapModelFactory.produce((List<Object[]>) source, unknownArrayCollection(type, list(validatedColumns)),
					credential);
		}

		return dynamicMapModelFactory.produceSingular((List<Object>) source, unknownArrayCollection(type, list(validatedColumns)),
				credential);
	}

	protected <T extends Entity> Map<String, Object> resolveReadResult(Class<T> type, Object source,
			String[] validatedColumns, Credential credential, SourceMetadata<T> sourceMetadata)
			throws UnauthorizedCredential {
		if (source.getClass().isArray()) {
			return dynamicMapModelFactory.produce((Object[]) source, sourceMetadata, credential);
		}

		return dynamicMapModelFactory.produceSingular(source, sourceMetadata, credential);
	}

	protected <T extends Entity> Map<String, Object> resolveReadResult(Class<T> type, Object source,
			Collection<String> validatedColumns, Credential credential) throws UnauthorizedCredential {
		if (source.getClass().isArray()) {
			return dynamicMapModelFactory.produce((Object[]) source, unknownArray(type, list(validatedColumns)),
					credential);
		}

		return dynamicMapModelFactory.produceSingular(source, unknownArray(type, list(validatedColumns)), credential);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns,
			Pageable pageable, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return read(type, columns, pageable, credential, unknownArrayCollection(type, list(columns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns,
			Pageable pageable, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		String[] validatedColumns = from(getDefaultColumns(type, credential, columns));
		List<Object[]> rows = repository.fetch(type, validatedColumns, pageable);

		if (rows.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}

		return resolveReadResults(type, rows, validatedColumns, credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return find(type, requestedColumns, spec, credential, unknownArray(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = getDefaultColumns(type, credential, requestedColumns);
		Optional<Tuple> optionalRow = genericSpecificationExecutor.findOne(type, getSelections(validatedColumns), spec);
		Tuple row = optionalRow.get();

		if (row == null) {
			return null;
		}

		return resolveReadResult(type, row.toArray(), from(validatedColumns), credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return read(type, requestedColumns, spec, credential, unknownArrayCollection(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = getDefaultColumns(type, credential, requestedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Pageable pageable, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return read(type, requestedColumns, spec, pageable, credential,
				unknownArrayCollection(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Pageable pageable, Credential credential, SourceMetadata<T> metadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = getDefaultColumns(type, credential, requestedColumns);
		Page<Tuple> page = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec, pageable);

		return resolveReadResults(type, toRows(page.getContent()), from(validatedColumns), credential, metadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Sort sort, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return read(type, requestedColumns, spec, sort, credential,
				unknownArrayCollection(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Sort sort, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = getDefaultColumns(type, credential, requestedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, getSelections(validatedColumns), spec, sort);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return readByAssociation(type, associatingType, associatingAttribute, associationProperty,
				associationIdentifier, columns, pageable, credential, unknownArrayCollection(type, list(columns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = getDefaultColumns(type, credential, columns);

		try {
			List<?> rows = repository.find(String.format("""
					SELECT %s FROM %s e WHERE e.%s.%s=:associationIdentifier
					""", validatedColumns.stream().map(this::prependAlias).collect(Collectors.joining(",")),
					getEntityName(type), associatingAttribute,
					!StringHelper.hasLength(associationProperty) ? getIdentifierPropertyName(associatingType)
							: associationProperty),
					Map.of("associationIdentifier", associationIdentifier));

			return resolveReadResults(type, rows, from(validatedColumns), credential, sourceMetadata);
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
	public <T extends Entity> List<String> getDefaultColumns(Class<T> type, Credential credential,
			Collection<String> columns) throws NoSuchFieldException {
		if (columns.isEmpty()) {
			DomainEntityMetadata<T> metadata = modelContext.getMetadata(type);

			return new ArrayList<>(metadata.getNonLazyPropertyNames());
		}

		return new ArrayList<>(dynamicMapModelFactory.validateColumns(type, columns, credential));
	}

	public <T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential, Function<Collection<String>, Selections<T>> selectionResolver)
			throws NoSuchFieldException, UnauthorizedCredential {
		if (selectionResolver == null) {
			return read(type, requestedColumns, spec, credential);
		}

		Collection<String> validatedColumns = getDefaultColumns(type, credential, requestedColumns);
		Selections<T> selections = selectionResolver.apply(validatedColumns);
		List<Tuple> tuples = genericSpecificationExecutor.findAll(type, selections, spec);

		return resolveReadResults(type, toRows(tuples), from(validatedColumns), credential,
				unknownArrayCollection(type, list(validatedColumns)));
	}

	private String prependAlias(String columnName) {
		return "e." + columnName;
	}

}
