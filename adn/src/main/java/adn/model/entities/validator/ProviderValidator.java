/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.hasLength;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.dao.generic.Result;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Provider;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Provider.class)
public class ProviderValidator extends AbstractPermanentEntityValidator<Provider> {
	// @formatter:off
	private static final String INVALID_ADDRESS = String.format(
			"%s and can only contain alphabetic, numeric, %s characters and %s",
			Common.notEmpty("Address"),
			Common.symbolNamesOf(
					'\s', '.', ',', '-', '_',
					'(', ')', '*', '/', '@', '\\'),
			hasLength(null, null, _Provider.MAXIMUM_ADDRESS_LENGTH));
	// @formatter:on
	private static final String INVALID_EMAIL = Common.invalid("email");
	private static final String WEBSITE_LENGTH_EXCEEDED = hasLength("Website address", null,
			_Provider.WEBSITE_MAX_LENGTH);
	// @formatter:off
	private static final String INVALID_REPRESENTATOR_NAME = String.format(
			"Representator's name can only contain alphabetic, numeric, %s characters and %s",
			Common.symbolNamesOf('\s', '.', ',', '-', '_', '(', ')', '\'', '"'),
			hasLength(null, null, _Provider.MAXIMUM_REPRESENTATOR_NAME_LENGTH));
	// @formatter:on
	private static final String TOO_MANY_PHONE_NUMBERS = "Too many phone numbers";
	private static final String MISSING_PHONENUMERS = Common.notEmpty("Phone numbers");

	@Override
	public Result<Provider> isSatisfiedBy(Session session, Serializable id, Provider instance) {
		Result<Provider> result = super.isSatisfiedBy(session, id, instance);

		if (!StringHelper.hasLength(instance.getAddress())
				|| !_Provider.ADDRESS_PATTERN.matcher(instance.getAddress()).matches()) {
			result.bad().getMessages().put(_Provider.address, INVALID_ADDRESS);
		}

		if (StringHelper.hasLength(instance.getWebsite())
				&& instance.getWebsite().length() > _Provider.WEBSITE_MAX_LENGTH) {
			result.bad().getMessages().put(_Provider.website, WEBSITE_LENGTH_EXCEEDED);
		}

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.bad().getMessages().put(_Provider.email, INVALID_EMAIL);
		}

		List<String> phoneNumbers = instance.getPhoneNumbers();

		if (phoneNumbers == null || phoneNumbers.isEmpty()) {
			result.bad().getMessages().put(_Provider.phoneNumbers, MISSING_PHONENUMERS);
		} else {
			if (phoneNumbers.stream().map(phoneNumber -> phoneNumber.length()).reduce(0,
					(left, right) -> left + right) > _Provider.PHONENUMBERS_MAX_LENGTH) {
				result.bad().getMessages().put(_Provider.phoneNumbers, TOO_MANY_PHONE_NUMBERS);
			} else {
				phoneNumbers.forEach(number -> {
					if (!StringHelper.isAcceptablePhoneNumber(number)) {
						result.bad().getMessages().compute(_Provider.phoneNumbers,
								(k, v) -> !StringHelper.hasLength(v)
										? String.format("Invalid phone number(s): %s", number)
										: v.concat(String.format(", %s" + number)));
					}
				});
			}
		}

		if (StringHelper.hasLength(instance.getRepresentatorName())
				&& !_Provider.REPRESENTATOR_NAME_PATTERN.matcher(instance.getRepresentatorName()).matches()) {
			result.bad().getMessages().put(_Provider.representatorName, INVALID_REPRESENTATOR_NAME);
		}

		return result;
	}

}
