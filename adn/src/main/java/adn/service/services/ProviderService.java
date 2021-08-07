/**
 * 
 */
package adn.service.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import adn.controller.query.ProviderQuery;
import adn.dao.generic.Repository;
import adn.helpers.StringHelper;
import adn.model.entities.Provider;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Service
public class ProviderService extends AbstractFactorService<Provider> {

	public ProviderService(CRUDServiceImpl crudService, Repository repository,
			AuthenticationBasedModelFactory modelFactory,
			AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory,
			DepartmentBasedModelPropertiesFactory departmentBasedPropertiesFactory) {
		super(crudService, repository, modelFactory, authenticationBasedPropertiesFactory,
				departmentBasedPropertiesFactory);
	}

	public Page<Map<String, Object>> search(Collection<String> requestedColumns, Pageable pageable,
			ProviderQuery restQuery, UUID departmentId) throws NoSuchFieldException {
		return findAll(Provider.class, requestedColumns, hasId(restQuery).or(hasNameLike(restQuery)), pageable,
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

	@Override
	public Optional<Provider> findOne(Specification<Provider> spec) {
		return findOne(Provider.class, spec);
	}

	@Override
	public List<Provider> findAll(Specification<Provider> spec) {
		return findAll(Provider.class, spec);
	}

	@Override
	public Page<Provider> findAll(Specification<Provider> spec, Pageable pageable) {
		return findAll(Provider.class, spec, pageable);
	}

	@Override
	public List<Provider> findAll(Specification<Provider> spec, Sort sort) {
		return findAll(Provider.class, spec);
	}

	@Override
	public long count(Specification<Provider> spec) {
		return count(Provider.class, spec);
	}

}
