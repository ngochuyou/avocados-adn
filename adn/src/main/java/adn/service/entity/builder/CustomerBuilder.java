/**
 * 
 */
package adn.service.entity.builder;

import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Customer.class)
public class CustomerBuilder extends AccountBuilder<Customer> {

	@Override
	protected <E extends Customer> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setAddress(StringHelper.normalizeString(model.getAddress()));
		target.setPrestigePoint(model.getPrestigePoint());

		return target;
	}

}
