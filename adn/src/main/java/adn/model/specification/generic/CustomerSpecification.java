/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.dao.DatabaseInteractionResult;
import adn.model.Generic;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Customer.class)
public class CustomerSpecification extends AccountSpecification<Customer> {

	@Override
	public DatabaseInteractionResult<Customer> isSatisfiedBy(Serializable id, Customer instance) {
		DatabaseInteractionResult<Customer> result = super.isSatisfiedBy(id, instance);

		if (instance.getPrestigePoint() < 0) {
			result.bad().getMessages().put("prestigePoint", "Prestige point can not be negative");
		}

		return result;
	}

}
