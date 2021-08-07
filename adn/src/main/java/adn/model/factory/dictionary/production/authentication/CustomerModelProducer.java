package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Customer;

@Component
@Generic(entityGene = Customer.class)
public class CustomerModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Customer> {

	@Override
	protected Map<String, Object> produceForCustomer(Customer account, Map<String, Object> model) {
		model.put("address", account.getAddress());
		model.put("prestigePoint", account.getPrestigePoint());

		return model;
	}

	@Override
	protected Map<String, Object> produceForAdmin(Customer entity, Map<String, Object> model) {
		return produceForCustomer(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(Customer entity, Map<String, Object> model) {
		return produceForCustomer(entity, model);
	}

}
