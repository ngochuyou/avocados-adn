/**
 * 
 */
package adn.service.services;

import static adn.application.context.ContextProvider.getPrincipalRole;
import static adn.helpers.ArrayHelper.from;
import static adn.helpers.EntityUtils.getEntityName;
import static adn.helpers.EntityUtils.getIdentifierPropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	private final ModelContextProvider modelContext;
	private final AbstractRepository repository;
	private final EntityBuilderProvider entityBuilderProvider;

	private final AuthenticationBasedModelPropertiesFactory propertiesFactory;

	private static final String GENERIC_ALIAS = "e";

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
		this.propertiesFactory = authenticationBasedModelPropertiesFactory;
		this.modelContext = modelContext;
	}

	public CRUDServiceImpl() {
		ApplicationContext context = ContextProvider.getApplicationContext();
		
		this.repository = context.getBean(AbstractRepository.class);
		this.entityBuilderProvider = context.getBean(EntityBuilderProvider.class);
		this.propertiesFactory = context.getBean(AuthenticationBasedModelPropertiesFactory.class);
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
		String[] validatedColumns = from(getDefaultColumnsOrTranslate(type, role, columns));
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
		Collection<String> validatedColumns = getDefaultColumnsOrTranslate(type, role, columns);
		List<?> rows = repository.find(
				String.format("""
						SELECT %s FROM %s e WHERE e.%s.%s=:associationIdentifier
						""", validatedColumns.stream().map(this::prependAlias).collect(Collectors.joining(",")),
						getEntityName(type), associatingAttribute, getIdentifierPropertyName(associatingType)),
				Map.of("associationIdentifier", associationIdentifier));

		return resolveReadResult(type, rows, from(validatedColumns), role);
	}

	protected <T extends Entity> Collection<String> getDefaultColumnsOrTranslate(Class<T> type, Role role,
			Collection<String> columns) throws NoSuchFieldException {
		if (columns.size() == 0) {
			EntityMetadata metadata = modelContext.getMetadata(type);

			return metadata.getNonLazyPropertyNames();
		}

		return propertiesFactory.validateAndTranslateColumnNames(type, role, columns);
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns)
			throws NoSuchFieldException {
		return find(id, type, columns, getPrincipalRole());
	}

	@Override
	public <T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			Role role) throws NoSuchFieldException {
		String[] validatedColumns = from(getDefaultColumnsOrTranslate(type, role, columns));
		Object[] row = repository.findById(id, type, validatedColumns);

		if (row == null) {
			return null;
		}

		return propertiesFactory.produce(type, row, validatedColumns, role);
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
		entityBuilder.updateBuild(id, entity, persistence);

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
			return propertiesFactory.produce(type, (List<Object[]>) source, validatedColumns, role);
		}

		return propertiesFactory.singularProduce(type, (List<Object>) source, validatedColumns[0], role);
	}

}
