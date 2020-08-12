/**
 * 
 */
package adn.service.generic;

import org.springframework.stereotype.Service;

import adn.model.Genetized;
import adn.model.entities.Customer;
import adn.service.GenericService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(gene = Customer.class)
public class CustomerService implements GenericService<Customer> {

	@Override
	public Customer doProcedure(Customer model) {
		// TODO Auto-generated method stub
		model.setAddress(Strings.normalizeString(model.getAddress()));

		return model;
	}

}
