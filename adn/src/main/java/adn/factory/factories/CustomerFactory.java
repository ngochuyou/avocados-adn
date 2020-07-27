/**
 * 
 */
package adn.factory.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.factory.EMFactory;
import adn.factory.EMProductionException;
import adn.factory.Factory;
import adn.model.entities.Customer;
import adn.model.models.CustomerModel;

/**
 * @author Ngoc Huy
 *
 */
@Component
@EMFactory(entityClass = Customer.class, modelClass = CustomerModel.class)
public class CustomerFactory implements Factory<Customer, CustomerModel> {

	@SuppressWarnings("rawtypes")
	@Autowired
	private AccountFactory accountFactory;

	@SuppressWarnings("unchecked")
	@Override
	public Customer produceEntity(CustomerModel model, Class<Customer> clazz) throws EMProductionException {
		// TODO Auto-generated method stub
		Customer customer = (Customer) accountFactory.produceEntity(model, clazz);

		customer.setAddress(model.getAddress());
		customer.setPrestigePoint(model.getPrestigePoint());

		return serviceManager.getService(clazz).doProcedure(customer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CustomerModel produceModel(Customer entity, Class<CustomerModel> clazz) {
		// TODO Auto-generated method stub
		CustomerModel model = (CustomerModel) accountFactory.produceModel(entity, clazz);

		model.setAddress(entity.getAddress());
		model.setPrestigePoint(entity.getPrestigePoint());

		return model;
	}

}
