/**
 * 
 */
package adn.service.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import adn.controller.query.ProviderQuery;
import adn.dao.generic.Repository;
import adn.dao.specification.GenericFactorRepository;
import adn.helpers.StringHelper;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Service
public class ProviderService extends AbstractFactorService<Provider> {

	@Autowired
	public ProviderService(GenericCRUDService crudService, Repository repository,
			GenericFactorRepository factorRepository) {
		super(crudService, repository, factorRepository);
	}

	public List<Map<String, Object>> search(Collection<String> requestedColumns, Pageable pageable,
			ProviderQuery restQuery, UUID departmentId) throws NoSuchFieldException {
		return crudService.read(Provider.class, requestedColumns, hasId(restQuery).or(hasNameLike(restQuery)), pageable,
				departmentId);
	}

	private static Specification<Provider> hasId(ProviderQuery restQuery) {
		return new Specification<Provider>() {
			@Override
			public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getId() == null || restQuery.getId().getEquals() == null) {
					return null;
				}

				return builder.equal(root.get("id"), restQuery.getId().getEquals());
			}
		};
	}

	private static Specification<Provider> hasNameLike(ProviderQuery restQuery) {
		return new Specification<Provider>() {
			@Override
			public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getName() == null || !StringHelper.hasLength(restQuery.getName().getLike())) {
					return null;
				}

				return builder.like(root.get("name"), restQuery.getName().getLike());
			}
		};
	}

}
