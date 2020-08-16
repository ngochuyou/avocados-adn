package adn.model.factory.production.security;

import adn.model.entities.Account;
import adn.model.models.AccountModel;

public class AccountModelProducer<AM extends AccountModel, A extends Account>
		implements ModelProducerBasedOnAuthentication<AM, A> {

	/**
	 * {@link Account} -> {@link AccountModel} for ANONYMOUS Authentication
	 */
	@Override
	public AM produce(A entity, AM model) {
		// TODO Auto-generated method stub
		model.setUsername(entity.getId());
		model.setFirstName(entity.getFirstName());
		model.setLastName(entity.getLastName());
		model.setPhoto(entity.getPhoto());
		model.setRole(entity.getRole().name());
		model.setGender(entity.getGender().name());
		model.setPassword(null);

		return model;
	}

	@Override
	public AM produceForAdminAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		model = produce(entity, model);
		model.setEmail(entity.getEmail());
		model.setPhone(entity.getPhone());

		return model;
	}

	@Override
	public AM produceForCustomerAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}

	@Override
	public AM produceForPersonnelAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}

}
