/**
 * 
 */
package adn.model.specification.generic;

import static adn.model.entities.Provider.WEBSITE_MAX_LENGTH;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Provider.class)
public class ProviderSpecification extends FactorSpecification<Provider> {

	private static final String WEBSITE_LENGTH_EXCEEDED = String.format("Website length must not exceed %d characters",
			WEBSITE_MAX_LENGTH);
	private static final String REPRESENTATOR_NAME_PATTERN = String.format("^[%s\\p{L}\\s\\.\\_\\-]+$",
			StringHelper.VIETNAMESE_CHARACTERS);

	@Override
	public Result<Provider> isSatisfiedBy(Session session, Provider instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<Provider> isSatisfiedBy(Session session, Serializable id, Provider instance) {
		Result<Provider> result = super.isSatisfiedBy(session, id, instance);

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.bad().getMessages().put("email", "Invalid email pattern");
		}

		if (!StringHelper.hasLength(instance.getAddress())) {
			result.bad().getMessages().put("address", "Address could not be empty");
		}

		if (StringHelper.hasLength(instance.getRepresentatorName())
				&& !instance.getRepresentatorName().matches(REPRESENTATOR_NAME_PATTERN)) {
			result.bad().getMessages().put("representatorName", "Invalid name");
		}

		if (StringHelper.hasLength(instance.getWebsite()) && instance.getWebsite().length() > WEBSITE_MAX_LENGTH) {
			result.bad().getMessages().put("website", WEBSITE_LENGTH_EXCEEDED);
		}
		
		if (instance.getPhoneNumbers() == null || instance.getPhoneNumbers().isEmpty()) {
			result.bad().getMessages().put("phoneNumbers", "Must provide at least one phone number");
		} else {
			instance.getPhoneNumbers().forEach(number -> {
				if (!StringHelper.isAcceptablePhoneNumber(number)) {
					result.bad().getMessages().compute("phoneNumbers",
							(k, v) -> !StringHelper.hasLength(v) ? String.format("Invalid phone number(s): %s", number)
									: v.concat(String.format(", %s" + number)));
				}
			});
		}

		return result;
	}

}
