package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Customer;
import adn.model.models.CustomerModel;

@Component("customerExtractor")
@Generic(entityGene = Customer.class)
public class CustomerExtractor extends AccountExtractor<Customer, CustomerModel> {

	@Override
	public Customer extract(CustomerModel model, Customer customer) {
		customer = super.extract(model, customer);
		customer.setAddress(model.getAddress());
		customer.setPrestigePoint(model.getPrestigePoint());

		return customer;
	}

//	@Override
//	public <E extends Customer> E merge(Customer model, E target) {
//		target = super.merge(model, target);
//		target.setAddress(model.getAddress());
//		target.setPrestigePoint(model.getPrestigePoint());
//
//		return target;
//	}

}
