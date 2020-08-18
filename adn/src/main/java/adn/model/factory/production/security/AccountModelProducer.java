package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.security.SecuredFor;
import adn.utilities.Role;

@Component
@Genetized(modelGene = AccountModel.class)
public class AccountModelProducer<AM extends AccountModel, A extends Account>
		implements AuthenticationBasedModelProducer<AM, A> {

	/**
	 * {@link Account} -> {@link AccountModel} for ANONYMOUS Authentication
	 */
	@Override
	@SecuredFor
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
	@SecuredFor(role = Role.ADMIN)
	public AM produceForAdminAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		model = this.produce(entity, model);
		model.setEmail(entity.getEmail());
		model.setPhone(entity.getPhone());

		return model;
	}

	@Override
	@SecuredFor(role = Role.CUSTOMER)
	public AM produceForCustomerAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

	@Override
	@SecuredFor(role = Role.PERSONNEL)
	public AM produceForPersonnelAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

}
