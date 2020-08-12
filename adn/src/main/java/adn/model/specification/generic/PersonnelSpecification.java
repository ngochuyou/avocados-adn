/**
 * 
 */
package adn.model.specification.generic;

import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.model.Result;
import adn.model.entities.Account;
import adn.model.entities.Personnel;
import adn.model.specification.GenericSpecification;
import adn.model.specification.TransactionalSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Personnel.class)
public class PersonnelSpecification implements TransactionalSpecification<Personnel> {

	@Transactional
	@Override
	public Result<Personnel> isSatisfiedBy(Personnel instance) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Account> root = query.from(Account.class);

		query.select(builder.count(root)).where(builder.equal(root.get("id"), instance.getCreatedBy()));

		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) == 0) {
			return Result.error(HttpStatus.BAD_GATEWAY.ordinal(), instance, Map.of("createdBy", "Created by can not be empty"));
		}

		return Result.success(instance);
	}

}
