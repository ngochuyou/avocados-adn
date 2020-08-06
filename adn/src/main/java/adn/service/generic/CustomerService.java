/**
 * 
 */
package adn.service.services;

import org.springframework.stereotype.Service;

import adn.model.entities.Customer;
import adn.service.ApplicationGenericService;
import adn.service.GenericService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@GenericService(target = Customer.class)
public class CustomerService implements ApplicationGenericService<Customer> {

	@Override
	public Customer doProcedure(Customer model) {
		// TODO Auto-generated method stub
		model.setAddress(Strings.normalizeString(model.getAddress()));

		return model;
	}

}
