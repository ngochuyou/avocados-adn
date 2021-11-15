/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import adn.helpers.CollectionHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Provider.class)
public class ProviderBuilder extends AbstractPermanentEntityBuilder<Provider> {

	@Override
	protected <E extends Provider> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setAddress(normalizeString(model.getAddress()));
		target.setEmail(model.getEmail());

		List<String> phoneNumbers = model.getPhoneNumbers();
		// @formatter:off
		target.setPhoneNumbers(
				!CollectionHelper.isEmpty(phoneNumbers) ?
						phoneNumbers
							.stream().filter(StringHelper::hasLength)
							.map(String::trim)
							.collect(Collectors.toList()) :
					null);
		// @formatter:on
		target.setWebsite(model.getWebsite());
		target.setRepresentatorName(normalizeString(model.getRepresentatorName()));

		return target;
	}

}
