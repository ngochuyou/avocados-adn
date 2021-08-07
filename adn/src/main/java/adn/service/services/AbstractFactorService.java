/**
 * 
 */
package adn.service.services;

import static adn.helpers.ArrayHelper.from;
import static adn.helpers.EntityUtils.getEntityName;
import static adn.model.entities.Factor.ACTIVE_FIELD_NAME;
import static adn.service.internal.Role.ADMIN;
import static adn.service.internal.Role.PERSONNEL;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.springframework.data.domain.Pageable;

import adn.application.context.ContextProvider;
import adn.dao.generic.Repository;
import adn.helpers.ArrayHelper;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.Factor;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.internal.Role;
import adn.service.internal.Service;
import adn.service.specification.AbstractSpecificationExecutor;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractFactorService<E extends Factor> extends AbstractSpecificationExecutor<E> implements Service {

	protected final CRUDServiceImpl crudService;
	protected final Repository repository;
	protected final AuthenticationBasedModelFactory modelFactory;

	public AbstractFactorService(CRUDServiceImpl crudService, Repository repository,
			AuthenticationBasedModelFactory modelFactory,
			AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory,
			DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory) {
		super(ContextProvider.getBean(SessionFactory.class), crudService, authenticationBasedPropertiesFactory,
				departmentBasedPropertiesFactory);
		this.crudService = crudService;
		this.repository = repository;
		this.modelFactory = modelFactory;
	}

	public <T extends Factor> Long countWithActiveState(Class<T> type, Role principalRole) {
		if (principalRole == Role.ADMIN || principalRole == Role.PERSONNEL) {
			return repository.count(type);
		}

		return repository.count(String.format("""
					SELECT COUNT(*) FROM %s WHERE %d=:active
				""", getEntityName(type), ACTIVE_FIELD_NAME), Map.of(ACTIVE_FIELD_NAME, Boolean.TRUE)).get(0);
	}

	public <T extends Factor> Map<String, Object> readWithActiveCheck(Serializable id, Class<T> type,
			Collection<String> columns, Role principalRole) throws NoSuchFieldException {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, principalRole, columns);

		if (principalRole == ADMIN || principalRole == PERSONNEL) {
			Map<String, Object> row = crudService.find(id, type, validatedColumns, principalRole);

			return row;
		}

		Map.Entry<Collection<String>, Boolean> appendResult = ArrayHelper.appendIfAbsent(columns, ACTIVE_FIELD_NAME);
		Map<String, Object> row = crudService.find(id, type, appendResult.getKey(), principalRole);

		Object active = row.get(ACTIVE_FIELD_NAME);

		if (active == null) {
			return null;
		}

		return !((boolean) active) ? null : appendResult.getValue() ? returnRequestedColumns(row, columns) : row;
	}

	public <T extends Factor> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable paging,
			Role principalRole) throws NoSuchFieldException {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, principalRole, columns);

		if (principalRole == ADMIN || principalRole == PERSONNEL) {
			return crudService.read(type, validatedColumns, paging, principalRole);
		}

		List<?> rows = repository.find(String.format("SELECT %s FROM %s WHERE %s=:%s",
				validatedColumns.stream().collect(Collectors.joining(",")), getEntityName(type), ACTIVE_FIELD_NAME,
				ACTIVE_FIELD_NAME), paging, Map.of(ACTIVE_FIELD_NAME, Boolean.TRUE));

		return crudService.resolveReadResult(type, rows, from(validatedColumns), principalRole);
	}

	/**
	 * Very simple active-only read, doesn't support <code>null</code> parameter in
	 * <code>additionalWhereClauseParameters</code>
	 */
	protected <T extends Factor> List<Map<String, Object>> readActiveRows(Class<T> type,
			Collection<String> requestedColumns, String additionalWhereClauseMember,
			Map<String, Object> additionalWhereClauseParameters, Pageable paging, Role role)
			throws NoSuchFieldException {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, role, requestedColumns);
		// @formatter:off
		HashMap<String, Object> criterias = new HashMap<>();
		
		criterias.put(ACTIVE_FIELD_NAME, Boolean.TRUE);
		criterias.putAll(additionalWhereClauseParameters);
		
		List<?> rows = repository.find(
				String.format("SELECT %s FROM %s WHERE %s=:%s %s",
						validatedColumns.stream().collect(Collectors.joining(",")),
						getEntityName(type), ACTIVE_FIELD_NAME, ACTIVE_FIELD_NAME,
						StringHelper.hasLength(additionalWhereClauseMember) ? "AND " + additionalWhereClauseMember : ""),
				criterias);
		// @formatter:on
		return crudService.resolveReadResult(type, rows, from(validatedColumns), role);
	}

	private Map<String, Object> returnRequestedColumns(Map<String, Object> row, Collection<String> requestedColumns) {
		return requestedColumns.stream().map(colName -> Utils.Entry.entry(colName, row.get(colName))).collect(
				HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
				HashMap::putAll);
	}

}
