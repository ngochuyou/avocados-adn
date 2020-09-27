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

import adn.model.Genetized;
import adn.model.Result;
import adn.model.entities.Account;
import adn.model.entities.Personnel;
import adn.model.specification.TransactionalSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Personnel.class)
public class PersonnelSpecification extends AccountSpecification<Personnel>
		implements TransactionalSpecification<Personnel> {

	@Transactional
	@Override
	public Result<Personnel> isSatisfiedBy(Personnel instance) {
		// TODO Auto-generated method stub
		Result<Personnel> result = super.isSatisfiedBy(instance);

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Account> root = query.from(Account.class);

		query.select(builder.count(root)).where(builder.equal(root.get("id"), instance.getCreatedBy()));

		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) == 0) {
			result.getMessageSet().put("createdBy", "Invalid creator informations");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
