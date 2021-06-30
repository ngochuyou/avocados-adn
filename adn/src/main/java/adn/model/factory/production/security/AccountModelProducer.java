package adn.model.factory.production.security;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Account;

@Component
@Generic(entityGene = Account.class)
public class AccountModelProducer<T extends Account>
		extends AbstractCompositeAuthenticationBasedModelProducerImplementor<T> {

	@Override
	protected Map<String, Object> produceForAnonymous(T account, Map<String, Object> model) {
		model.put("username", account.getId());
		model.put("firstName", account.getFirstName());
		model.put("lastName", account.getLastName());
		model.put("photo", account.getPhoto());
		model.put("role", account.getPhoto());
		model.put("gender", account.getGender());
		model.put("active", account.isActive());

		return model;
	}

	@Override
	protected Map<String, Object> produceForCustomer(T entity, Map<String, Object> model) {
		return produceForAnonymous(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(T account, Map<String, Object> model) {
		model = produceForAnonymous(account, model);

		model.put("createdDate", account.getCreatedDate());
		model.put("updatedDate", account.getUpdatedDate());

		return model;
	}

	@Override
	protected Map<String, Object> produceForAdmin(T account, Map<String, Object> model) {
		model = produceForPersonnel(account, model);

		model.put("email", account.getEmail());
		model.put("phone", account.getPhone());

		return model;
	}

	@Override
	protected Map<String, Object> produceForEmployee(T entity, Map<String, Object> model) {
		return produceForPersonnel(entity, model);
	}

	@Override
	protected Map<String, Object> produceForManager(T entity, Map<String, Object> model) {
		return produceForPersonnel(entity, model);
	}
}
