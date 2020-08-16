package adn.model.factory.extraction;

import adn.model.entities.Customer;
import adn.model.factory.EntityExtractor;
import adn.model.models.CustomerModel;

public class CustomerExtractor implements EntityExtractor<Customer, CustomerModel> {

	@Override
	public Customer extract(CustomerModel model, Customer customer) {
		// TODO Auto-generated method stub
		customer.setAddress(model.getAddress());
		customer.setPrestigePoint(model.getPrestigePoint());

		return customer;
	}

}
