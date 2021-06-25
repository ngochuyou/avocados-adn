/**
 * 
 */
package adn.service.entity.builder;

import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Account;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Customer.class)
public class CustomerBuilder extends AccountBuilder<Customer> {

	@Override
	protected <E extends Account> E mandatoryBuild(E target, E model) {
		super.mandatoryBuild(target, model);

		Customer targetedRef = (Customer) target;
		Customer modelingRef = (Customer) model;

		targetedRef.setAddress(StringHelper.normalizeString(modelingRef.getAddress()));
		targetedRef.setPrestigePoint(modelingRef.getPrestigePoint());

		return target;
	}

}
