/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Factor;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Provider.class)
public class ProviderBuilder extends FactorBuilder<Provider> {

	@Override
	protected <E extends Factor> E mandatoryBuild(E target, E model) {
		super.mandatoryBuild(target, model);

		Provider targetRef = (Provider) target;
		Provider modelRef = (Provider) model;

		targetRef.setEmail(modelRef.getEmail());
		targetRef.setAddress(normalizeString(modelRef.getAddress()));
		// @formatter:off
		targetRef.setPhoneNumbers(
				modelRef.getPhoneNumbers() != null ?
					modelRef.getPhoneNumbers()
						.stream().map(StringHelper::normalizeString)
						.filter(Objects::nonNull).collect(Collectors.toSet()) :
					null);
		// @formatter:on
		targetRef.setRepresentatorName(normalizeString(modelRef.getRepresentatorName()));

		return super.mandatoryBuild(target, model);
	}

}
