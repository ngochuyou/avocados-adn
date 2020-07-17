/**
 * 
 */
package adn.model.specification.specifications;

import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.model.ModelResult;
import adn.model.entities.Account;
import adn.model.entities.Personnel;
import adn.model.specification.CompositeSpecification;
import adn.model.specification.TransactionalSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class PersonnelSpecification extends CompositeSpecification<Personnel>
		implements TransactionalSpecification<Personnel> {

	@Transactional
	@Override
	public ModelResult<Personnel> isSatisfiedBy(Personnel instance) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Account> root = query.from(Account.class);

		query.select(builder.count(root)).where(builder.equal(root.get("id"), instance.getCreatedBy()));

		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) == 0) {
			return ModelResult.error(Set.of(400), instance, Map.of("createdBy", "Created by can not be empty"));
		}

		return ModelResult.success(instance);
	}

}
