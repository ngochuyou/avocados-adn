package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Customer;
import adn.model.factory.GenericEntityExtractor;
import adn.model.models.CustomerModel;

@Genetized(entityGene = Customer.class)
public class CustomerExtractor implements GenericEntityExtractor<Customer, CustomerModel> {

	@Override
	public Customer extract(CustomerModel model, Customer customer) {
		// TODO Auto-generated method stub
		customer.setAddress(model.getAddress());
		customer.setPrestigePoint(model.getPrestigePoint());

		return customer;
	}

	@Override
	public <E extends Customer> E map(Customer model, E target) {
		// TODO Auto-generated method stub
		target.setAddress(model.getAddress());
		target.setPrestigePoint(model.getPrestigePoint());

		return target;
	}

}
