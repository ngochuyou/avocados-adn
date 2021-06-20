package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Customer;
import adn.model.models.CustomerModel;
import adn.security.SecuredFor;
import adn.service.services.Role;

@Component
@Genetized(modelGene = CustomerModel.class)
public class CustomerModelProducer implements AuthenticationBasedModelProducer<Customer, CustomerModel> {

	@Override
	@SecuredFor(role = Role.ADMIN)
	public CustomerModel produceForAdminAuthentication(Customer entity, CustomerModel model) {
		// TODO Auto-generated method stub
		model.setAddress(entity.getAddress());
		model.setPrestigePoint(entity.getPrestigePoint());

		return model;
	}

	@Override
	@SecuredFor(role = Role.CUSTOMER)
	public CustomerModel produceForCustomerAuthentication(Customer entity, CustomerModel model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}

	@Override
	@SecuredFor(role = Role.PERSONNEL)
	public CustomerModel produceForPersonnelAuthentication(Customer entity, CustomerModel model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}

}
