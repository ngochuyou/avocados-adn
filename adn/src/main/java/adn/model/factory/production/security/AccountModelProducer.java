package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.helpers.Utils;
import adn.model.Generic;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.security.SecuredFor;
import adn.service.internal.Role;

@Component
@Generic(modelGene = AccountModel.class)
public class AccountModelProducer<T extends Account, M extends AccountModel>
		implements AuthenticationBasedModelProducer<T, M> {

	@Override
	@SecuredFor
	public M produceForAnonymous(T entity, M model) {
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
		model = this.produceForAnonymous(entity, model);
		model.setEmail(entity.getEmail());
		model.setPhone(entity.getPhone());
		model.setCreatedDate(Utils.localDateTimeToDate(entity.getCreatedDate()));
		model.setUpdatedDate(Utils.localDateTimeToDate(entity.getUpdatedDate()));

		return model;
	}

	@Override
	@SecuredFor(role = Role.CUSTOMER)
	public M produceForCustomerAuthentication(T entity, M model) {
		return this.produceForAnonymous(entity, model);
	}

	@Override
	@SecuredFor(role = Role.PERSONNEL)
	public M produceForPersonnelAuthentication(T entity, M model) {
		model = this.produceForAnonymous(entity, model);

		model.setCreatedDate(Utils.localDateTimeToDate(entity.getCreatedDate()));
		model.setUpdatedDate(Utils.localDateTimeToDate(entity.getUpdatedDate()));

		return model;
	}

}
