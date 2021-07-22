/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.dao.DatabaseInteractionResult;
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

	@Override
	public DatabaseInteractionResult<Provider> isSatisfiedBy(Serializable id, Provider instance) {
		DatabaseInteractionResult<Provider> result = super.isSatisfiedBy(id, instance);

		if (!StringHelper.isEmail(instance.getEmail())) {
			result.bad().getMessages().put("email", "Invalid email pattern");
		}

		if (!StringHelper.hasLength(instance.getAddress())) {
			result.bad().getMessages().put("address", "Address could not be empty");
		}

		if (StringHelper.hasLength(instance.getRepresentatorName())
				&& !instance.getRepresentatorName().matches("^[\\p{L}\\s\\.\\_\\-]+$")) {
			result.bad().getMessages().put("representatorName", "Invalid name");
		}

		instance.getPhoneNumbers().forEach(number -> {
			if (!StringHelper.isAcceptablePhoneNumber(number)) {
				result.bad().getMessages().compute("phoneNumbers",
						(k, v) -> !StringHelper.hasLength(v) ? String.format("Invalid phone number(s): %s", number)
								: v.concat(String.format(", %s" + number)));
			}
		});

		return result;
	}

}
