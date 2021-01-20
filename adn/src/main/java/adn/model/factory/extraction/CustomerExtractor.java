package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Customer;
import adn.model.models.CustomerModel;

@Component("customerExtractor")
@Genetized(entityGene = Customer.class)
public class CustomerExtractor extends AccountExtractor<Customer, CustomerModel> {

	@Override
	public Customer extract(CustomerModel model, Customer customer) {
		// TODO Auto-generated method stub
		customer = super.extract(model, customer);
		customer.setAddress(model.getAddress());
		customer.setPrestigePoint(model.getPrestigePoint());

		return customer;
	}

	@Override
	public <E extends Customer> E merge(Customer model, E target) {
		// TODO Auto-generated method stub
		target = super.merge(model, target);
		target.setAddress(model.getAddress());
		target.setPrestigePoint(model.getPrestigePoint());

		return target;
	}

}
