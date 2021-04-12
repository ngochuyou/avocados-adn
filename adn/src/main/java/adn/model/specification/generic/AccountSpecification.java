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

import adn.helpers.StringHelper;
import adn.model.Genetized;
import adn.model.Result;
import adn.model.entities.Account;
import adn.model.specification.TransactionalSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Account.class)
public class AccountSpecification<T extends Account> extends EntitySpecification<T>
		implements TransactionalSpecification<T> {

	@Override
	public Result<T> isSatisfiedBy(T instance) {
		// TODO Auto-generated method stub
		Result<T> result = super.isSatisfiedBy(instance);

		if (instance.getId() == null || instance.getId().length() < 8 || instance.getId().length() > 255) {
			result.getMessageSet().put("id", "Id length must be between 8 and 255");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.getMessageSet().put("email", "Invalid email");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		} else {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> query = builder.createQuery(Long.class);
			Root<Account> root = query.from(Account.class);

			query.select(builder.count(root)).where(builder.and(builder.equal(root.get("email"), instance.getEmail()),
					builder.notEqual(root.get("id"), instance.getId())));

			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				result.getMessageSet().put("email", "Email is already taken");
				result.setStatus(HttpStatus.BAD_REQUEST.value());
			}
		}

		if (StringHelper.hasLength(instance.getPhone()) && !StringHelper.isDigits(instance.getPhone())) {
			result.getMessageSet().put("phone", "Invalid phone number");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		if (!StringHelper.isBCrypt(instance.getPassword())) {
			result.getMessageSet().put("password", "Invalid password");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		if (instance.getRole() == null) {
			result.getMessageSet().put("role", "Role can not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		if (instance.getGender() == null) {
			result.getMessageSet().put("gender", "Gender can not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
