/**
 * 
 */
package adn.service.services;

import static adn.helpers.ArrayHelper.EMPTY_STRING_ARRAY;

import java.io.Serializable;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import adn.application.context.ContextProvider;
import adn.dao.AbstractRepository;
import adn.dao.DatabaseInteractionResult;
import adn.helpers.EntityUtils;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;
import adn.model.entities.metadata.EntityMetadata;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
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

	protected final ModelContextProvider modelContext;
	protected final AbstractRepository repository;
	protected final EntityBuilderProvider entityBuilderProvider;

	protected final AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory;
	protected final AuthenticationBasedModelFactory authenticationBasedModelFactory;

	// @formatter:off
	@Autowired
	public CRUDServiceImpl(
			AbstractRepository baseRepository,
			EntityBuilderProvider entityBuilderProvider,
			AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory,
			AuthenticationBasedModelFactory authenticationBasedModelFactory,
			ModelContextProvider modelContext) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
		this.authenticationBasedModelPropertiesFactory = authenticationBasedModelPropertiesFactory;
		this.authenticationBasedModelFactory = authenticationBasedModelFactory;
		this.modelContext = modelContext;
	}

	public CRUDServiceImpl() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		
		this.repository = context.getBean(AbstractRepository.class);
		this.entityBuilderProvider = context.getBean(EntityBuilderProvider.class);
		this.authenticationBasedModelPropertiesFactory = context.getBean(AuthenticationBasedModelPropertiesFactory.class);
		this.authenticationBasedModelFactory = context.getBean(AuthenticationBasedModelFactory.class);
		this.modelContext = context.getBean(ModelContextProvider.class);
	}
	// @formatter:on
	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, String[] columns,
			Pageable pageable) throws SQLSyntaxErrorException {
		return read(type, columns, pageable, EMPTY_STRING_ARRAY);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, String[] columns,
			Pageable pageable, String[] groupByColumns) throws SQLSyntaxErrorException {
		return read(type, columns, pageable, groupByColumns, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, String[] columns,
			Pageable pageable, String[] groupByColumns, Role role) throws SQLSyntaxErrorException {
		String[] validatedColumns = getDefaultColumnsOrTranslate(type, role, columns);
		List<Object[]> rows = repository.fetch(type, validatedColumns, pageable, groupByColumns);

		if (rows.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}

		if (rows.get(0).length == 0) {
			return new ArrayList<Map<String, Object>>();
		}

		return authenticationBasedModelPropertiesFactory.produce(type, rows, validatedColumns, role);
	}

	public <T extends Entity, E extends T> String[] validateAndTranslateColumnNames(Class<E> type, Role role,
			String[] columns) throws SQLSyntaxErrorException {
		return authenticationBasedModelPropertiesFactory.validateAndTranslateColumnNames(type, role, columns);
	}

	public <T extends Entity, E extends T> String[] getDefaultColumnsOrTranslate(Class<E> type, Role role,
			String[] columns) throws SQLSyntaxErrorException {
		if (columns.length == 0) {
			EntityMetadata metadata = modelContext.getMetadata(type);

			return metadata.getNonLazyPropertyNames().toArray(new String[metadata.getNonLazyPropertiesSpan()]);
		}

		return authenticationBasedModelPropertiesFactory.validateAndTranslateColumnNames(type, role, columns);
	}

	@Override
	public <T extends Entity, E extends T> Map<String, Object> find(Serializable id, Class<E> type, String[] columns)
			throws SQLSyntaxErrorException {
		return find(id, type, columns, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends Entity, E extends T> Map<String, Object> find(Serializable id, Class<E> type, String[] columns,
			Role role) throws SQLSyntaxErrorException {
		String[] validatedColumns = getDefaultColumnsOrTranslate(type, role, columns);
		Object[] row = repository.findById(id, type, validatedColumns);

		if (row == null) {
			return null;
		}

		return authenticationBasedModelPropertiesFactory.produce(type, row, validatedColumns, role);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Pageable pageable)
			throws SQLSyntaxErrorException {
		return read(type, pageable, EMPTY_STRING_ARRAY);
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Pageable pageable,
			String[] groupByColumns) throws SQLSyntaxErrorException {
		return read(type, pageable, groupByColumns, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends Entity, E extends T> List<Map<String, Object>> read(Class<E> type, Pageable pageable,
			String[] groupByColumns, Role role) throws SQLSyntaxErrorException {
		List<E> rows = repository.fetch(type, pageable, groupByColumns);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return authenticationBasedModelFactory.produce(type, rows, role);
	}

	protected <T extends Entity> Serializable resolveId(Serializable id, T entity) {
		return id == null ? EntityUtils.getIdentifier(entity) : id;
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> create(Serializable id, E entity, Class<E> type,
			boolean flushOnFinish) {
		id = resolveId(id, entity);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for creation [%s#%s]", type.getName(), id));
		}

		entity = entityBuilder.insertionBuild(id, entity);

		return finish(ss, repository.insert(id, entity, type), flushOnFinish);
	}

	protected <E> DatabaseInteractionResult<E> finish(Session ss, DatabaseInteractionResult<E> result,
			boolean flushOnFinish) {
		if (flushOnFinish) {
			if (result.isOk()) {
				ss.flush();

				return result;
			}

			ss.clear();

			return result;
		}

		return result;
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> update(Serializable id, E entity, Class<E> type,
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
		persistence = entityBuilder.updateBuild(id, entity, persistence);

		return finish(ss, repository.update(id, persistence, type), flushOnFinish);
	}

	protected <T extends Entity, E extends T> String resolveGroupByClause(Class<E> type, Role role, String query,
			String[] groupByColumns) throws SQLSyntaxErrorException {
		if (groupByColumns.length == 0) {
			return query;
		}

		String[] validatedGroupByColumns = validateAndTranslateColumnNames(type, role, groupByColumns);

		return repository.appendGroupBy(query, validatedGroupByColumns);
	}

}
