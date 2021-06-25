/**
 * 
 */
package adn.model.specification.generic;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.model.DatabaseInteractionResult;
import adn.model.Generic;
import adn.model.entities.Factor;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Factor.class)
public class FactorSpecification<T extends Factor> extends EntitySpecification<T> {

	@Override
	@Transactional(readOnly = true)
	public DatabaseInteractionResult<T> isSatisfiedBy(T instance) {
		// TODO Auto-generated method stub
		DatabaseInteractionResult<T> result = super.isSatisfiedBy(instance);

		if (instance.getName() == null || instance.getName().length() == 0) {
			result.getMessages().put("name", "Name must not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<? extends Factor> root = query.from(instance.getClass());
		// UUID needs a different approach to check uniqueness by id
		// @formatter:off
		query
			.select(builder.count(root))
			.where(
				builder.and(
						builder.equal(root.get("name"), instance.getName()),
						instance.getId() == null ?
								builder.isNotNull(root.get("id")) :
									builder.notEqual(root.get("id"), instance.getId())));
		// @formatter:on
		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
			result.getMessages().put("name", "Name must be unique");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
