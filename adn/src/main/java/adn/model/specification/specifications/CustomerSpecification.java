/**
 * 
 */
package adn.model.specification.specifications;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.ModelResult;
import adn.model.entities.Customer;
import adn.model.specification.CompositeSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class CustomerSpecification extends CompositeSpecification<Customer> {

	@Override
	public ModelResult<Customer> isSatisfiedBy(Customer instance) {
		// TODO Auto-generated method stub
		return instance.getPrestigePoint() < 0 ? ModelResult.error(Set.of(400), instance,
				Map.of("prestigePoint", "Prestige point can not be negative")) : ModelResult.success(instance);
	}

}
