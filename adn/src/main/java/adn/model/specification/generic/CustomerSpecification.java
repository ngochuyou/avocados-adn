/**
 * 
 */
package adn.model.specification.generic;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.Result;
import adn.model.entities.Customer;
import adn.model.specification.GenericSpecification;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Customer.class)
public class CustomerSpecification implements Specification<Customer> {

	@Override
	public Result<Customer> isSatisfiedBy(Customer instance) {
		// TODO Auto-generated method stub
		return instance.getPrestigePoint() < 0 ? Result.error(Set.of(400), instance,
				Map.of("prestigePoint", "Prestige point can not be negative")) : Result.success(instance);
	}

}
