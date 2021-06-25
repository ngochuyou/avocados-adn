package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.helpers.Utils;
import adn.model.Generic;
import adn.model.entities.Admin;
import adn.model.models.AdminModel;
import adn.security.SecuredFor;
import adn.service.internal.Role;

@Component
@Generic(modelGene = AdminModel.class)
public class AdminModelProducer implements AuthenticationBasedModelProducer<Admin, AdminModel> {

	@Override
	@SecuredFor(role = Role.ADMIN)
	public AdminModel produceForAdminAuthentication(Admin entity, AdminModel model) {
		model.setContractDate(Utils.localDateTimeToDate(entity.getContractDate()));

		return model;
	}

}
