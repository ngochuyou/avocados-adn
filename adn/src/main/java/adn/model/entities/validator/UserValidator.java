/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.hasLength;
import static adn.application.Common.invalid;
import static adn.application.Common.symbolNamesOf;
import static adn.model.entities.metadata._User.NAME_PATTERN;

import java.io.Serializable;
import java.util.function.Function;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.application.Result;
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
public class UserValidator<T extends User> extends AbstractPermanentEntityValidator<T> {
	// @formatter:off
	private static final String INVALID_USERNAME = String.format(
			"Username can only contain alphabetic, numeric, %s characters and %s",
			symbolNamesOf('.', '-', '_', '@', '#', '$', '\'', '!', '*', '&'),
			hasLength(null, _User.MINIMUM_USERNAME_LENGTH, _User.MAXIMUM_USERNAME_LENGTH));
	private static final String INVALID_EMAIL = invalid("email");
//	private static final String TAKEN_EMAIL = "Email was taken";
	private static final String INVALID_FIRSTNAME;
	private static final String INVALID_LASTNAME;
	private static final String INVALID_PASSWORD = hasLength("Password", _User.MINIMUM_USERNAME_LENGTH, null);
	private static final String MISSING_ROLE = Common.notEmpty("Role information");
	private static final String INVALID_PHONE = invalid("Phone number");
	
	static {
		Function<String, String> nameMessageGetter = (nameType) -> String.format(
				"%s can only contain alphabetic, numeric, %s characters and %s",
				nameType,
				symbolNamesOf('\s', '.', '-', '\'', '(', ')'),
				hasLength(null, _User.MINIMUM_NAME_LENGTH, _User.MAXIMUM_NAME_LENGTH));
		
		INVALID_FIRSTNAME = nameMessageGetter.apply("Firstname");
		INVALID_LASTNAME = nameMessageGetter.apply("Lastname");
	}
	// @formatter:on
//	private final GenericRepository genericRepository;

//	@Autowired
//	public UserValidator(GenericRepository genericRepository) {
//		super();
//		this.genericRepository = genericRepository;
//	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (!_User.USERNAME_PATTERN.matcher(instance.getId()).matches()) {
			result.bad(_User._id, INVALID_USERNAME);
		}

		String email = instance.getEmail();

		if (StringHelper.hasLength(email)) {
			if (!StringHelper.isEmail(email)) {
				result.bad(_User.email, INVALID_EMAIL);
			}
			/*else {
				// @formatter:off
				if (genericRepository.count(User.class,
						(root, query, builder) -> builder.and(
								builder.equal(root.get(_User.email), email),
								builder.notEqual(root.get(_User.id), instance.getId()))) != 0) {
					result.bad(_User.email, TAKEN_EMAIL);
				}
				// @formatter:on
			}*/
		}

		if (!NAME_PATTERN.matcher(instance.getFirstName()).matches()) {
			result.bad(_User.firstName, INVALID_FIRSTNAME);
		}

		if (!NAME_PATTERN.matcher(instance.getLastName()).matches()) {
			result.bad(_User.lastName, INVALID_LASTNAME);
		}

		if (!StringHelper.isBCrypt(instance.getPassword())) {
			result.bad(_User.password, INVALID_PASSWORD);
		}

		if (instance.getRole() == null) {
			result.bad(_User.role, MISSING_ROLE);
		}

		if (!StringHelper.hasLength(instance.getPhone())
				|| !StringHelper.isAcceptablePhoneNumber(instance.getPhone())) {
			result.bad(_User.phone, INVALID_PHONE);
		}

		return result;
	}

}
