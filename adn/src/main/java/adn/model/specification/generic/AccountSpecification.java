/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.DatabaseInteractionResult;
import adn.model.Generic;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Account.class)
public class AccountSpecification<T extends Account> extends EntitySpecification<T> {

	private static final Pattern USERNAME_PATTERN;
	private static final short MINIMUM_USERNAME_LENGTH = 8;

	static {
		USERNAME_PATTERN = Pattern.compile(String.format("^[\\p{L}\\p{N}\\._]{%d,}$", MINIMUM_USERNAME_LENGTH));
	}

	@Override
	public DatabaseInteractionResult<T> isSatisfiedBy(Serializable id, T instance) {
		DatabaseInteractionResult<T> result = super.isSatisfiedBy(id, instance);

		if (!USERNAME_PATTERN.matcher(instance.getId()).matches()) {
			result.bad().getMessages().put(Account.ID_FIELD_NAME, "Invalid username pattern");
		}

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.bad().getMessages().put("email", "Invalid email");
		} else {
			Session session = getCurrentSession();
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> query = builder.createQuery(Long.class);
			Root<Account> root = query.from(Account.class);

			query.select(builder.count(root)).where(builder.and(builder.equal(root.get("email"), instance.getEmail()),
					builder.notEqual(root.get(Account.ID_FIELD_NAME), instance.getId())));

			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				result.bad().getMessages().put("email", "Email is already taken");
			}
		}

		if (StringHelper.hasLength(instance.getPhone()) && !StringHelper.isAcceptablePhoneNumber(instance.getPhone())) {
			result.bad().getMessages().put("phone", "Invalid phone number");
		}

		if (!StringHelper.isBCrypt(instance.getPassword())) {
			result.bad().getMessages().put("password", "Invalid password");
		}

		if (instance.getRole() == null) {
			result.bad().getMessages().put(Account.ROLE_FIELD_NAME, "Role can not be empty");
		}

		if (instance.getGender() == null) {
			result.bad().getMessages().put("gender", "Gender can not be empty");
		}

		if (instance.isActive() == null) {
			result.bad().getMessages().put(Account.ACTIVE_FIELD_NAME, "Active state must not be empty");
		}

		return result;
	}

}
