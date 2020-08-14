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

import adn.model.Result;
import adn.model.entities.Account;
import adn.model.specification.GenericSpecification;
import adn.model.specification.TransactionalSpecification;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Account.class)
public class AccountSpecification implements TransactionalSpecification<Account> {

	@Override
	public Result<Account> isSatisfiedBy(Account instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null || instance.getId().length() < 8 || instance.getId().length() > 255) {
			messageSet.put("id", "Id length must be between 8 and 255");
			flag = false;
		}

		if (!Strings.isEmail(instance.getEmail())) {
			messageSet.put("email", "Invalid email");
			flag = false;
		} else {
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> query = builder.createQuery(Long.class);
			Root<Account> root = query.from(Account.class);

			query.select(builder.count(root)).where(builder.and(builder.equal(root.get("email"), instance.getEmail()),
					builder.notEqual(root.get("id"), instance.getId())));

			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				messageSet.put("email", "Email is already taken");
				flag = false;
			}
		}

		if (!Strings.isEmpty(instance.getPhone()) && !Strings.isDigits(instance.getPhone())) {
			messageSet.put("phone", "Invalid phone number");
			flag = false;
		}

		if (!Strings.isBCrypt(instance.getPassword())) {
			messageSet.put("password", "Invalid password");
			flag = false;
		}

		if (instance.getRole() == null) {
			messageSet.put("role", "Role can not be empty");
			flag = false;
		}

		if (instance.getGender() == null) {
			messageSet.put("gender", "Gender can not be empty");
			flag = false;
		}

		return flag ? Result.success(instance) : Result.error(HttpStatus.BAD_REQUEST.value(), instance, messageSet);
	}

}
