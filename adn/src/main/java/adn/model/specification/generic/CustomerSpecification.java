/**
 * 
 */
package adn.model.specification.generic;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.DatabaseInteractionResult;
import adn.model.Genetized;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Customer.class)
public class CustomerSpecification extends AccountSpecification<Customer> {

	@Override
	public DatabaseInteractionResult<Customer> isSatisfiedBy(Customer instance) {
		DatabaseInteractionResult<Customer> result = super.isSatisfiedBy(instance);

		if (instance.getPrestigePoint() < 0) {
			result.getMessages().put("prestigePoint", "Prestige point can not be negative");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}
		
		return result;
	}

}
