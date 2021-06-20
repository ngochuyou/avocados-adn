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
import adn.model.specification.TransactionalSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Factor.class)
public class FactorSpecification<T extends Factor> extends EntitySpecification<T>
		implements TransactionalSpecification<T> {

	@Transactional
	@Override
	public DatabaseInteractionResult<T> isSatisfiedBy(T instance) {
		// TODO Auto-generated method stub
		DatabaseInteractionResult<T> result = super.isSatisfiedBy(instance);

		if (instance.getName() == null || instance.getName().length() == 0) {
			result.getMessages().put("name", "Name mustn't be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<?> root = query.from(instance.getClass());

		query.select(builder.count(root)).where(builder.and(builder.equal(root.get("name"), instance.getName()),
				builder.notEqual(root.get("id"), instance.getId())));

		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
			result.getMessages().put("name", "Name must be unique");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
