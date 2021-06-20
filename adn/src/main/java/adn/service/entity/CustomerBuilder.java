/**
 * 
 */
package adn.service.entity;

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
	public Customer defaultBuild(final Customer model) {
		Customer persistence = super.defaultBuild(model);

		persistence.setAddress(StringHelper.normalizeString(model.getAddress()));
		persistence.setAddress(model.getAddress());
		persistence.setPrestigePoint(model.getPrestigePoint());

		return persistence;
	}

}
