/**
 * 
 */
package adn.model.specification.generic;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.model.Result;
import adn.model.entities.Factor;
import adn.model.specification.GenericSpecification;
import adn.model.specification.TransactionalSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Factor.class)
public class FactorSpecification implements TransactionalSpecification<Factor> {

	@Transactional
	@Override
	public Result<Factor> isSatisfiedBy(Factor instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getName() == null || instance.getName().length() == 0) {
			messageSet.put("name", "Name mustn't be empty");
			flag = false;
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<?> root = query.from(instance.getClass());

		query.select(builder.count(root)).where(builder.and(builder.equal(root.get("name"), instance.getName()),
				builder.notEqual(root.get("id"), instance.getId())));

		if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
			messageSet.put("name", "Name must be unique");
			flag = false;
		}

		return flag ? Result.success(instance) : Result.error(HttpStatus.BAD_GATEWAY.ordinal(), instance, messageSet);
	}

}
