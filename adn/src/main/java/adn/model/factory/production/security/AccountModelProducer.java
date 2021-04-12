package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.helpers.Role;
import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.security.SecuredFor;

@Component
@Genetized(modelGene = AccountModel.class)
public class AccountModelProducer<T extends Account, M extends AccountModel>
		implements AuthenticationBasedModelProducer<T, M> {

	/**
	 * {@link AccountModel} -> {@link AccountModel} for ANONYMOUS Authentication
	 */
	@Override
	@SecuredFor
	public M produce(T entity, M model) {
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
	public M produceForAdminAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		model = this.produce(entity, model);
		model.setEmail(entity.getEmail());
		model.setPhone(entity.getPhone());

		return model;
	}

	@Override
	@SecuredFor(role = Role.CUSTOMER)
	public M produceForCustomerAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

	@Override
	@SecuredFor(role = Role.PERSONNEL)
	public M produceForPersonnelAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

}
