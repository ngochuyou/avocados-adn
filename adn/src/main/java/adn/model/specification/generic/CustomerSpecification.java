/**
 * 
 */
package adn.model.specification.generic;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.Result;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Customer.class)
public class CustomerSpecification extends AccountSpecification<Customer> {

	@Override
	public Result<Customer> isSatisfiedBy(Customer instance) {
		// TODO Auto-generated method stub
		Result<Customer> result = super.isSatisfiedBy(instance);

		if (instance.getPrestigePoint() < 0) {
			result.getMessageSet().put("prestigePoint", "Prestige point can not be negative");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
