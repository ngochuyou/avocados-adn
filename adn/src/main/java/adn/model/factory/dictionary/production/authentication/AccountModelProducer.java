package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.helpers.Utils;
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
		model.put("role", account.getRole());
		model.put("gender", account.getGender());
		model.put("active", account.isActive());
		model.put("password", null);

		return model;
	}

	@Override
	protected Map<String, Object> produceForCustomer(T entity, Map<String, Object> model) {
		return produceForAnonymous(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(T account, Map<String, Object> model) {
		model = produceForAnonymous(account, model);

		model.put("createdDate", Utils.formatLocalDate(account.getCreatedDate()));
		model.put("updatedDate", Utils.formatLocalDateTime(account.getUpdatedDate()));

		return model;
	}

	@Override
	protected Map<String, Object> produceForAdmin(T account, Map<String, Object> model) {
		model = produceForPersonnel(account, model);

		model.put("email", account.getEmail());
		model.put("phone", account.getPhone());

		return model;
	}

}
