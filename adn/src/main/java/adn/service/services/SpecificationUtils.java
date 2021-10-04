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
import adn.helpers.StringHelper;
import adn.model.entities.FullyAuditedEntity;
import adn.model.entities.metadata._FullyAuditedEntity;

/**
 * @author Ngoc Huy
 *
 */
public class SpecificationUtils {

	private SpecificationUtils() {}
	
	@SuppressWarnings("serial")
	public static <T extends FullyAuditedEntity<?>> Specification<T> hasNameLike(AbstractFactorQuery<T> restQuery) {
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
