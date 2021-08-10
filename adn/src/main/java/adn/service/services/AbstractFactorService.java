/**
 * 
 */
package adn.service.services;

import static adn.helpers.CollectionHelper.from;
import static adn.helpers.HibernateHelper.toRows;
import static adn.service.internal.Role.PERSONNEL;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.data.domain.Pageable;

import adn.dao.generic.Repository;
import adn.dao.specification.GenericFactorRepository;
import adn.model.entities.Factor;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractFactorService<T extends Factor> {

	protected final GenericCRUDService crudService;
	protected final Repository repository;
	protected final GenericFactorRepository genericFactorRepository;

	public AbstractFactorService(GenericCRUDService crudService, Repository repository,
			GenericFactorRepository factorRepository) {
		this.crudService = crudService;
		this.repository = repository;
		this.genericFactorRepository = factorRepository;
	}

	public Long countWithActiveCheck(Class<T> type, Role principalRole) {
		if (principalRole == Role.PERSONNEL) {
			return repository.count(type);
		}

		return genericFactorRepository.countActive(type);
	}

	public Map<String, Object> findWithActiveCheck(Serializable id, Class<T> type, Collection<String> columns,
			Role principalRole) throws NoSuchFieldException {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, principalRole, columns);

		if (principalRole == PERSONNEL) {
			Map<String, Object> row = crudService.find(id, type, validatedColumns, principalRole);

			return row;
		}

		Tuple row = genericFactorRepository.findActive(type, id, validatedColumns);

		return crudService.resolveReadResult(type, row.toArray(), from(validatedColumns), principalRole);
	}

	public List<Map<String, Object>> readWithActiveCheck(Class<T> type, Collection<String> columns, Pageable paging,
			Role principalRole) throws NoSuchFieldException {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, principalRole, columns);

		if (principalRole == PERSONNEL) {
			return crudService.read(type, validatedColumns, paging, principalRole);
		}

		List<Tuple> rows = genericFactorRepository.findAllActive(type, validatedColumns, paging);

		return crudService.resolveReadResults(type, toRows(rows), from(validatedColumns), principalRole);
	}

}
