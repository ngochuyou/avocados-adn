/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.DatabaseInteractionResult;
import adn.helpers.EntityUtils;
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
	public DatabaseInteractionResult<T> isSatisfiedBy(Serializable id, T instance) {
		// TODO Auto-generated method stub
		DatabaseInteractionResult<T> result = super.isSatisfiedBy(id, instance);

		if (instance.getName() == null || instance.getName().length() == 0) {
			result.bad().getMessages().put("name", "Name must not be empty");
		}

		Class<? extends T> persistentClass = EntityUtils.getPersistentClass(instance);
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<? extends T> root = query.from(persistentClass);
		String idPropertyName = EntityUtils.getIdentifierPropertyName(persistentClass);
		// UUID needs a different approach to check uniqueness by id
		// @formatter:off
		query
			.select(builder.count(root))
			.where(
				builder.and(
					builder.equal(root.get("name"), instance.getName()),
					id == null ?
						builder.isNotNull(root.get(idPropertyName)) :
							builder.notEqual(root.get(idPropertyName), id)));
		// @formatter:on
		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
			result.bad().getMessages().put("name", "Name was taken");
		}

		if (instance.isActive() == null) {
			result.bad().getMessages().put("active", "Active state must not be empty");
		}

		return result;
	}

}
