/**
 * 
 */
package adn.service.services;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import adn.controller.query.impl.AbstractFactorQuery;
import adn.dao.generic.GenericRepository;
import adn.helpers.StringHelper;
import adn.model.entities.FullyAuditedEntity;
import adn.model.entities.metadata._FullyAuditedEntity;
import adn.service.internal.Service;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class GenericFullyAuditedEntityService extends GenericPermanentEntityService implements Service {

	protected final GenericCRUDServiceImpl crudService;

	public GenericFullyAuditedEntityService(GenericRepository repository, GenericCRUDServiceImpl genericCrudService,
			GenericCRUDServiceImpl crudService) {
		super(repository, genericCrudService);
		this.crudService = crudService;
	}

	@SuppressWarnings("serial")
	protected static <T extends FullyAuditedEntity<?>> Specification<T> hasNameLike(AbstractFactorQuery<T> restQuery) {
		return new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getName() == null || !StringHelper.hasLength(restQuery.getName().getLike())) {
					return null;
				}

				return builder.like(root.get(_FullyAuditedEntity.name), restQuery.getName().getLike());
			}
		};
	}

}
