/**
 * 
 */
package adn.service.services;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static adn.helpers.CollectionHelper.from;
import static adn.helpers.CollectionHelper.list;
import static adn.helpers.HibernateHelper.toRows;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArray;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArrayCollection;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import adn.dao.generic.Repository;
import adn.dao.specification.GenericFactorRepository;
import adn.model.entities.Factor;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.DepartmentCredential;

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

	public Long countWithActiveCheck(Class<T> type, Credential credential, Credential inactiveAllowedCredential) {
		if (credential.equal(DepartmentCredential.SALE_CREDENTIAL)) {
			return repository.count(type);
		}

		return genericFactorRepository.countActive(type);
	}

	public Map<String, Object> findWithActiveCheck(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential, Credential inactiveAllowedCredential)
			throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, credential, columns);

		if (credential.equals(inactiveAllowedCredential)) {
			Map<String, Object> row = crudService.find(id, type, validatedColumns, credential);

			return row;
		}

		Tuple row = genericFactorRepository.findActive(type, id, validatedColumns);

		if (row == null) {
			return null;
		}

		return crudService.resolveReadResult(type, row.toArray(), from(validatedColumns), credential,
				unknownArray(type, list(validatedColumns)));
	}

	public List<Map<String, Object>> readWithActiveCheck(Class<T> type, Collection<String> columns, Pageable paging,
			Credential credential, Credential inactiveAllowedCredential)
			throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = crudService.getDefaultColumns(type, credential, columns);

		if (credential.equal(inactiveAllowedCredential)) {
			return crudService.read(type, validatedColumns, paging, getPrincipalCredential());
		}

		List<Tuple> rows = genericFactorRepository.findAllActive(type, validatedColumns, paging);

		return crudService.resolveReadResults(type, toRows(rows), from(validatedColumns), getPrincipalCredential(),
				unknownArrayCollection(type, list(validatedColumns)));
	}

	@SuppressWarnings("serial")
	protected static <T extends Factor, E extends T> Specification<E> isActive(Class<E> type) {
		return new Specification<E>() {
			@Override
			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				return builder.isTrue(root.get(Factor.ACTIVE_FIELD_NAME));
			}
		};
	}

}
