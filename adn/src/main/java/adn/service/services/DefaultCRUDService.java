/**
 * 
 */
package adn.service.services;

import static adn.helpers.ArrayHelper.EMPTY_STRING_ARRAY;
import static adn.helpers.EntityUtils.getIdentifier;

import java.io.Serializable;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import adn.application.context.ContextProvider;
import adn.dao.Repository;
import adn.model.AbstractModel;
import adn.model.DatabaseInteractionResult;
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
public class DefaultCRUDService implements CRUDService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ModelContextProvider modelContext;
	private final Repository repository;
	private final EntityBuilderProvider entityBuilderProvider;

	private final AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory;
	private final AuthenticationBasedModelFactory authenticationBasedModelFactory;

	// @formatter:off
	@Autowired
	public DefaultCRUDService(
			Repository baseRepository,
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
		if (columns.length == 0) {
			EntityMetadata metadata = modelContext.getMetadata(type);

			columns = metadata.getNonLazyPropertyNames().toArray(new String[metadata.getNonLazyPropertiesSpan()]);
		}

		validateAttributes(type, columns, groupByColumns);

		List<Object[]> rows = repository.fetch(type, columns, pageable, groupByColumns);

		if (rows.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}

		if (rows.get(0).length == 0) {
			return new ArrayList<Map<String, Object>>();
		}

		return authenticationBasedModelPropertiesFactory.produce(type, rows, columns, role);
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
		validateAttributes(type, groupByColumns);

		List<E> rows = repository.fetch(type, pageable, groupByColumns);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return authenticationBasedModelFactory.produce(type, rows, role);
	}

	private <T extends AbstractModel> void validateAttributes(Class<T> type, String[]... requestedAttributes)
			throws SQLSyntaxErrorException {
		Set<String> registeredAttributes = modelContext.getMetadata(type).getPropertyNames();

		for (String[] attributes : requestedAttributes) {
			for (String attribute : attributes) {
				if (!registeredAttributes.contains(attribute)) {
					throw new SQLSyntaxErrorException(String.format("Unknown property [%s]", attribute));
				}
			}
		}
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> create(E entity, Class<E> type) {
		return create(getIdentifier(entity), entity, type);
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> update(E entity, Class<E> type) {
		return update(getIdentifier(entity), entity, type);
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> remove(E entity, Class<E> type) {
		return remove(getIdentifier(entity), entity, type);
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> create(Serializable id, E entity,
			Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for creation [%s#%s]", type.getName(), id));
		}

		entityBuilder.insertionBuild(id, entity);

		return repository.insert(id, entity, type);
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> update(Serializable id, E entity,
			Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for update [%s#%s]", type.getName(), id));
		}

		entityBuilder.updateBuild(id, entity);

		return repository.update(id, getCurrentSession().load(type, id), type);
	}

	@Override
	public <T extends Entity, E extends T> DatabaseInteractionResult<E> remove(Serializable id, E entity,
			Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for deactivation [%s#%s]", type.getName(), id));
		}

		entityBuilder.deactivationBuild(id, entity);

		return repository.update(id, getCurrentSession().load(type, id), type);
	}

}
