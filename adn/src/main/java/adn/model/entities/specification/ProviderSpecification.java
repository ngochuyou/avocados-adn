/**
 * 
 */
package adn.model.entities.specification;

import static adn.helpers.StringHelper.VIETNAMESE_CHARACTERS;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
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
public class ProviderSpecification extends FactorSpecification<Provider> {

	private static final String WEBSITE_LENGTH_EXCEEDED = String.format("Website length must not exceed %d characters",
			_Provider.WEBSITE_MAX_LENGTH);
	private static final String REPRESENTATOR_NAME_PATTERN = String.format("^[%s\\p{L}\\p{N}\\s\\.\\_\\-]+$",
			VIETNAMESE_CHARACTERS);
	private static final String ADDRESS_PATTERN = String.format("^[%s\\p{L}\\p{N}\\s\\.\\,\\-\\_\\(\\)\\*/]{1,255}$",
			VIETNAMESE_CHARACTERS);

	@Override
	public Result<Provider> isSatisfiedBy(Session session, Provider instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<Provider> isSatisfiedBy(Session session, Serializable id, Provider instance) {
		Result<Provider> result = super.isSatisfiedBy(session, id, instance);

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.bad().getMessages().put(_Provider.email, "Invalid email pattern");
		}

		if (!StringHelper.hasLength(instance.getAddress()) || !instance.getAddress().matches(ADDRESS_PATTERN)) {
			result.bad().getMessages().put(_Provider.address,
					"Address could not be empty and can only contain alphabetic characters, numbers, spaces, '.', ',', '-', '_', '(', ')'");
		}
		// ^[%s\\p{L}\\p{N}\\s\\.\\_\\-]+$
		if (StringHelper.hasLength(instance.getRepresentatorName())
				&& !instance.getRepresentatorName().matches(REPRESENTATOR_NAME_PATTERN)) {
			result.bad().getMessages().put(_Provider.representatorName,
					"Representator name can only contain alphabetic characters, numbers, spaces, '.', '_', '-'");
		}

		if (StringHelper.hasLength(instance.getWebsite())
				&& instance.getWebsite().length() > _Provider.WEBSITE_MAX_LENGTH) {
			result.bad().getMessages().put(_Provider.website, WEBSITE_LENGTH_EXCEEDED);
		}

		List<String> phoneNumbers = instance.getPhoneNumbers();

		if (phoneNumbers == null || phoneNumbers.isEmpty()) {
			result.bad().getMessages().put(_Provider.phoneNumbers, "Must provide at least one phone number");
		} else {
			if (phoneNumbers.stream().map(phoneNumber -> phoneNumber.length()).reduce(0,
					(left, right) -> left + right) > _Provider.PHONENUMBERS_MAX_LENGTH) {
				result.bad().getMessages().put(_Provider.phoneNumbers, "Too many phone numbers");
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

		return result;
	}

}
