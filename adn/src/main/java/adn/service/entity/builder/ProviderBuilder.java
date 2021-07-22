/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Provider.class)
public class ProviderBuilder extends FactorBuilder<Provider> {

	@Override
	protected <E extends Provider> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setEmail(model.getEmail());
		target.setAddress(normalizeString(model.getAddress()));
		// @formatter:off
		target.setPhoneNumbers(
				model.getPhoneNumbers() != null ?
						model.getPhoneNumbers()
						.stream().filter(StringHelper::hasLength)
						.map(String::trim)
						.collect(Collectors.toSet()) :
					null);
		// @formatter:on
		target.setRepresentatorName(normalizeString(model.getRepresentatorName()));

		return target;
	}

}
