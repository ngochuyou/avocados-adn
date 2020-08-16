package adn.model.factory.production.security;

import adn.model.entities.Customer;
import adn.model.models.CustomerModel;

public class CustomerModelProducer implements ModelProducerBasedOnAuthentication<CustomerModel, Customer> {

	@Override
	public CustomerModel produceForAdminAuthentication(Customer entity, CustomerModel model) {
		// TODO Auto-generated method stub
		model.setAddress(entity.getAddress());
		model.setPrestigePoint(entity.getPrestigePoint());

		return model;
	}

}
