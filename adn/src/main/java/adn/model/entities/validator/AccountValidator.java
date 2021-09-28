/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.User;
import adn.model.entities.metadata._User;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = User.class)
public class AccountValidator<T extends User> extends AbstractPermanentEntityValidator<T> {

	private static final Pattern USERNAME_PATTERN;
	private static final int MINIMUM_USERNAME_LENGTH = 8;
	private static final Pattern NAME_PATTERN;

	static {
		USERNAME_PATTERN = Pattern.compile(String.format("^[\\p{L}\\p{N}\\._]{%d,}$", MINIMUM_USERNAME_LENGTH));
		NAME_PATTERN = Pattern
				.compile(String.format("^[\\p{L}\\p{N}\\._\\-\\!\\@%s]{0,255}$", StringHelper.VIETNAMESE_CHARACTERS));
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (!USERNAME_PATTERN.matcher(instance.getId()).matches()) {
			result.bad().getMessages().put("username", "Invalid username pattern");
		}

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.bad().getMessages().put(_User.email, "Invalid email");
		} else {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> query = builder.createQuery(Long.class);
			Root<User> root = query.from(User.class);

			query.select(builder.count(root)).where(builder.and(builder.equal(root.get("email"), instance.getEmail()),
					builder.notEqual(root.get(_User.id), instance.getId())));

			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				result.bad().getMessages().put(_User.email, "Email is already taken");
			}
		}

		if (!NAME_PATTERN.matcher(instance.getFirstName()).matches()) {
			result.bad().getMessages().put(_User.firstName,
					"Firstname can only contain alphabetic, numeric characters or '.', '_', '-', '!', '@'");
		}

		if (!NAME_PATTERN.matcher(instance.getLastName()).matches()) {
			result.bad().getMessages().put(_User.lastName,
					"Lastname can only contain alphabetic, numeric characters or '.', '_', '-', '!', '@'");
		}

		if (!StringHelper.isBCrypt(instance.getPassword())) {
			result.bad().getMessages().put(_User.password, "Invalid password");
		}

		if (instance.getRole() == null) {
			result.bad().getMessages().put(_User.role, "Role can not be empty");
		}

		if (instance.getGender() == null) {
			result.bad().getMessages().put(_User.gender, "Gender can not be empty");
		}

		if (instance.isActive() == null) {
			result.bad().getMessages().put(_User.active, "Active state must not be empty");
		}

		if (!StringHelper.hasLength(instance.getPhone())
				|| !StringHelper.isAcceptablePhoneNumber(instance.getPhone())) {
			result.bad().getMessages().put(_User.phone, "Invalid phone number");
		}

		return result;
	}

}