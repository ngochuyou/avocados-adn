/**
 * 
 */
package adn.service.services;

import static adn.application.Result.bad;
import static adn.application.Result.failed;
import static adn.application.context.ContextProvider.getCurrentSession;
import static adn.dao.generic.ResultBatch.bad;
import static adn.dao.generic.ResultBatch.failed;
import static adn.dao.generic.ResultBatch.ok;
import static adn.helpers.CollectionHelper.list;
import static adn.helpers.HibernateHelper.getIdentifier;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArray;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArrayCollection;
import static adn.service.internal.Service.Status.BAD;
import static adn.service.internal.Service.Status.FAILED;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.FlushMode;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.application.context.builders.EntityBuilderProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.dao.generic.GenericRepositoryImpl;
import adn.dao.generic.ResultBatch;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Entity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.SourceMetadataFactory;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.entity.builder.EntityBuilder;
import adn.service.internal.GenericCRUDService;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Primary
public final class GenericCRUDServiceImpl implements GenericCRUDService {

	private static final Logger logger = LoggerFactory.getLogger(GenericCRUDServiceImpl.class);

	private final ModelContextProvider modelContext;
	private final EntityBuilderProvider entityBuilderProvider;

	protected final GenericRepositoryImpl repository;
	protected final DynamicMapModelProducerFactory dynamicMapModelFactory;

	public static final String EXECUTOR_NAME = "GenericCRUDServiceBatchExecutor";
	private static final int MAXIMUM_BATCHSIZE_IN_SINGULAR_PROCESS = 100;
	private static final int MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS = 50;
	static final int MAXIMUM_BATCH_SIZE = 1000;
	static final String MAXIMUM_BATCH_SIZE_EXCEEDED = String.format("Maximum elements in one batch is %d",
			MAXIMUM_BATCH_SIZE);
	private static final String INVALID_CONSTRAINT = "Invalid constraint";

	@Autowired
	private BatchWorker batchWorker;

	// @formatter:off
	@Autowired
	public GenericCRUDServiceImpl(
			GenericRepositoryImpl baseRepository,
			EntityBuilderProvider entityBuilderProvider,
			ModelContextProvider modelContext,
			DynamicMapModelProducerFactory dynamicMapModelFactory) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
		this.modelContext = modelContext;
		this.dynamicMapModelFactory = dynamicMapModelFactory;
	}

	public GenericCRUDServiceImpl() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		
		this.repository = context.getBean(GenericRepositoryImpl.class);
		this.entityBuilderProvider = context.getBean(EntityBuilderProvider.class);
		this.modelContext = context.getBean(ModelContextProvider.class);
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
		return create(id, entity, type, getCurrentSession(), flushOnFinish);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> create(Serializable id, E entity, Class<E> type, Session session,
			boolean flushOnFinish) {
		try {
			id = resolveId(id, entity);
			HibernateHelper.useManualSession(session);

			EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Building entity for creation [%s#%s]", type.getName(), id));
			}

			entity = entityBuilder.buildInsertion(id, entity, session);

			Result<E> validation = repository.validate(type, id, entity, session);

			if (!validation.isOk()) {
				return validation;
			}

			entity = entityBuilder.buildPostValidationOnInsert(id, entity, session);
			session.save(entity);

			return finish(session, Result.ok(entity), flushOnFinish);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failed(e.getMessage());
		}
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
			return ResultBatch.bad(MAXIMUM_BATCH_SIZE_EXCEEDED);
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

			batchWorker.doCreateBatch(session, idBatch, entityBatch, statusSet, resultSet, type, builder, startIndex,
					endIndex, latch);
		});

		if (amountInLastBatch != 0) {
			int startIndex = biggestFullBatchAmount * MAXIMUM_ELEMENTS_PER_PARALLEL_PROCESS;
			int endIndex = startIndex + amountInLastBatch;

			batchWorker.doCreateBatch(session, idBatch, entityBatch, statusSet, resultSet, type, builder, startIndex,
					endIndex, latch);
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
			logger.debug(String.format("Executing singularly creation on a batch with entities of type [%s] using [%s]",
					type.getName(), builder.getClass().getName()));
		}

		final EnumSet<Status> statusBatch = EnumSet.noneOf(Status.class);

		return finish(session, IntStream.range(0, entityBatch.size()).mapToObj(index -> {
			Serializable id = idBatch.get(index);
			E entity = builder.buildInsertion(id, entityBatch.get(index));
			Result<E> validation = repository.validate(type, id, entity, session);

			if (!validation.isOk()) {
				return validation;
			}

			entity = builder.buildPostValidationOnInsert(id, entity);
			session.save(entity);
			statusBatch.add(validation.getStatus());

			return Result.ok(entity);
		}).collect(Collectors.toList()), statusBatch, flushOnFinish);
	}

	<E> Result<E> finish(Result<E> result, boolean flushOnFinish) {
		return finish(getCurrentSession(), result, flushOnFinish);
	}

	<E> Result<E> finish(Session ss, Result<E> result, boolean flushOnFinish) {
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
					return bad(INVALID_CONSTRAINT);
				}

				any.printStackTrace();
				return failed(any.getMessage());
			}
		}

		return result;
	}

	<E> ResultBatch<E> finish(List<Result<E>> resultBatch, EnumSet<Status> statusBatch, boolean flushOnFinish) {
		return finish(getCurrentSession(), resultBatch, statusBatch, flushOnFinish);
	}

	<E> ResultBatch<E> finish(Session ss, List<Result<E>> resultBatch, EnumSet<Status> statusBatch,
			boolean flushOnFinish) {
		boolean hasFailed = statusBatch.contains(FAILED);
		boolean isOk = !hasFailed && !statusBatch.contains(BAD);

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
					return ResultBatch.<E>bad(Arrays.asList(Result.bad(INVALID_CONSTRAINT)));
				}

				any.printStackTrace();
				return failed(resultBatch).setMessage(any.getMessage());
			}
		}

		return isOk ? ok(resultBatch) : hasFailed ? failed(resultBatch) : bad(resultBatch);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Serializable id, E model, Class<E> type,
			boolean flushOnFinish) {
		try {
			id = resolveId(id, model);

			Session session = getCurrentSession();

			session.setHibernateFlushMode(FlushMode.MANUAL);

			E persistence = session.load(type, id);
			EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Building entity for update [%s#%s]", type.getName(), id));
			}
			// persistence takes effects during updateBuild,
			// assigning it to the return of updateBuild is just for the sake of it
			entityBuilder.buildUpdate(id, model, persistence);

			Result<E> validation = repository.validate(type, id, persistence, session);

			if (!validation.isOk()) {
				return validation;
			}

			persistence = session.load(type, id);
			session.save(persistence);

			return finish(session, Result.ok(persistence), flushOnFinish);
		} catch (Exception e) {
			return Result.failed(e.getMessage());
		}
	}

	@Component
	public class BatchWorker {

		private static final String EXECUTION_LOG_TEMPLATE = "Executing creation batch in [%s]";

		@Async(GenericCRUDServiceImpl.EXECUTOR_NAME)
		@Transactional
		<T extends Entity> CompletableFuture<Void> doCreateBatch(
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
			final CountDownLatch latch) {
			// @formatter:on
			if (logger.isDebugEnabled()) {
				logger.debug(String.format(EXECUTION_LOG_TEMPLATE, Thread.currentThread().getName()));
			}

			IntStream.range(start, end).forEach(index -> {
				try {
					Serializable id = idBatch.get(index);
					T entity = builder.buildInsertion(id, entityBatch.get(index));
					Result<T> validation = repository.validate(type, id, entity, session);

					if (!validation.isOk()) {
						results[index] = validation;
						statusSet.add(validation.getStatus());

						return;
					}

					entity = builder.buildPostValidationOnInsert(id, entity);
					session.save(entity);
					results[index] = Result.ok(entity);
					statusSet.add(Status.OK);
				} catch (Exception any) {
					any.printStackTrace();
					results[index] = Result.failed(any.getMessage());
					statusSet.add(Status.FAILED);
				}
			});

			latch.countDown();

			return new CompletableFuture<>();
		}

	}

	/*
	 * ============================Credential MIGRATION============================
	 */

	@Override
	public <T extends Entity> Map<String, Object> readById(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readById(id, type, credential, unknownArray(type, list(columns)));
	}

	@Override
	public <T extends Entity> Map<String, Object> readById(Serializable id, Class<T> type, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential {
		sourceMetadata = optionallyValidate(type, credential, sourceMetadata);

		Optional<Object[]> optional = repository.findById(type, id, sourceMetadata.getColumns());

		if (optional.isEmpty()) {
			return null;
		}

		return dynamicMapModelFactory.produce(optional.get(), sourceMetadata, credential);
	}

	protected <T extends Entity> List<Map<String, Object>> resolveReadResults(Class<T> type, List<Object[]> source,
			Credential credential, SourceMetadata<T> sourceMetadata) throws UnauthorizedCredential {
		return dynamicMapModelFactory.produce(source, sourceMetadata, credential);
	}

	protected <T extends Entity> Map<String, Object> resolveReadResult(Class<T> type, Object[] source,
			Credential credential, SourceMetadata<T> sourceMetadata) throws UnauthorizedCredential {
		return dynamicMapModelFactory.produce((Object[]) source, sourceMetadata, credential);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> columns,
			Pageable pageable, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readAll(type, pageable, credential, unknownArrayCollection(type, list(columns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Pageable pageable, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential {
		sourceMetadata = optionallyValidate(type, credential, sourceMetadata);

		List<Object[]> rows = repository.findAll(type, sourceMetadata.getColumns(), pageable);

		if (rows.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}

		return resolveReadResults(type, rows, credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> Map<String, Object> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return read(type, spec, credential, unknownArray(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> Map<String, Object> read(Class<T> type, Specification<T> spec, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential {
		sourceMetadata = optionallyValidate(type, credential, sourceMetadata);

		Optional<Object[]> optional = repository.findOne(type, sourceMetadata.getColumns(), spec);

		if (optional.isEmpty()) {
			return null;
		}

		return resolveReadResult(type, optional.get(), credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readAll(type, spec, credential, unknownArrayCollection(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec,
			Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		sourceMetadata = optionallyValidate(type, credential, sourceMetadata);

		List<Object[]> rows = repository.findAll(type, sourceMetadata.getColumns(), spec);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return resolveReadResults(type, rows, credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Pageable pageable, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return readAll(type, spec, pageable, credential, unknownArrayCollection(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec, Pageable pageable,
			Credential credential, SourceMetadata<T> metadata) throws NoSuchFieldException, UnauthorizedCredential {
		metadata = optionallyValidate(type, credential, metadata);

		List<Object[]> rows = repository.findAll(type, metadata.getColumns(), spec, pageable);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return resolveReadResults(type, rows, credential, metadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Sort sort, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return readAll(type, spec, sort, credential, unknownArrayCollection(type, list(requestedColumns)));
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec, Sort sort,
			Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential {
		sourceMetadata = optionallyValidate(type, credential, sourceMetadata);

		List<Object[]> rows = repository.findAll(type, sourceMetadata.getColumns(), spec, sort);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return resolveReadResults(type, rows, credential, sourceMetadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Credential credential, Function<SourceMetadata<T>, List<Object[]>> sourceProducer)
			throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<T> metadata = optionallyValidate(type, credential,
				SourceMetadataFactory.unknownArrayCollection(type, list(requestedColumns)));
		List<Object[]> rows = sourceProducer.apply(metadata);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return resolveReadResults(type, rows, credential, metadata);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAllByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Credential credential)
			throws NoSuchFieldException, Exception {
		return readAllByAssociation(type, associatingType, associatingAttribute, associationProperty,
				associationIdentifier, columns, pageable, credential, null);
	}

	@Override
	public <T extends Entity> List<Map<String, Object>> readAllByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Pageable pageable, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, Exception {
		return readAllByAssociation(type, associatingType, associatingAttribute, associationProperty,
				associationIdentifier, pageable, credential, sourceMetadata, null);
	}

	// @formatter:off
	@Override
	public <T extends Entity> List<Map<String, Object>> readAllByAssociation(
			Class<T> type,
			Class<? extends Entity> associatingType,
			String associatingAttribute,
			String associationProperty,
			Serializable associationIdentifier,
			Collection<String> columns,
			Pageable pageable,
			Credential credential,
			Specification<T> spec) throws NoSuchFieldException, UnauthorizedCredential {
		return readAllByAssociation(type, associatingType, associatingAttribute, associationProperty,
				associationIdentifier, pageable, credential, unknownArrayCollection(type, list(columns)), spec);
	}
	
	@Override
	@SuppressWarnings("serial")
	public <T extends Entity> List<Map<String, Object>> readAllByAssociation(
			Class<T> type,
			Class<? extends Entity> associatingType,
			String associatingAttribute,
			String associationProperty,
			Serializable associationIdentifier,
			Pageable pageable,
			Credential credential,
			SourceMetadata<T> sourceMetadata,
			Specification<T> spec)
			throws NoSuchFieldException, UnauthorizedCredential {
		sourceMetadata = optionallyValidate(type, credential, sourceMetadata);

		try {
			// @formatter:off
			List<Object[]> rows = repository.findAll(type, sourceMetadata.getColumns(), new Specification<T>() {
				@Override
				public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					return builder.equal(
							root.get(associatingAttribute)
								.get(!StringHelper.hasLength(associationProperty)
										? HibernateHelper.getIdentifierPropertyName(associatingType)
												: associationProperty),
							associationIdentifier);
				}
			}.and(spec), pageable);
			// @formatter:on
			if (rows.isEmpty()) {
				return new ArrayList<>();
			}

			return resolveReadResults(type, rows, credential, sourceMetadata);
		} catch (Exception any) {
			// the association property is not checked so QueryException
			// will be thrown when the association property column does not exist
			if (any.getCause() instanceof QueryException) {
				throw new NoSuchFieldException(String.format("Unknown columns %s", associationProperty));
			}

			throw any;
		}
	}

	// @formatter:on
	<T extends Entity> SourceMetadata<T> optionallyValidate(Class<T> type, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential {
		List<String> columns = sourceMetadata.getColumns();

		if (CollectionHelper.isEmpty(columns)) {
			DomainEntityMetadata<T> metadata = modelContext.getMetadata(type);

			sourceMetadata.setColumns(metadata.getNonLazyPropertyNames());

			return sourceMetadata;
		}

		sourceMetadata.setColumns(dynamicMapModelFactory.validateColumns(type, columns, credential));

		return sourceMetadata;
	}

}
