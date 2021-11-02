/**
 * 
 */
package adn.service.services;

import java.io.Serializable;

import org.springframework.data.jpa.domain.Specification;

import adn.controller.query.impl.AbstractFactorQuery;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Entity;
import adn.model.entities.FullyAuditedEntity;
import adn.model.entities.metadata._FullyAuditedEntity;

/**
 * @author Ngoc Huy
 *
 */
public class SpecificationFactory {

	private SpecificationFactory() {}

	public static <T extends FullyAuditedEntity<?>> Specification<T> hasNameLike(AbstractFactorQuery<T> restQuery) {
		return (root, query, builder) -> {
			if (restQuery.getName() == null || !StringHelper.hasLength(restQuery.getName().getLike())) {
				return null;
			}

			return builder.like(root.get(_FullyAuditedEntity.name), restQuery.getName().getLike());
		};
	}

	public static <T extends Entity> Specification<T> hasId(Class<T> type, Serializable id) {
		return (root, query, builder) -> builder.equal(root.get(HibernateHelper.getIdentifierPropertyName(type)), id);
	}

	public static <T extends Entity> Specification<T> hasId(String identifierName, Serializable id) {
		return (root, query, builder) -> builder.equal(root.get(identifierName), id);
	}

}
