package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Admin;
import adn.model.models.AdminModel;
import adn.security.SecuredFor;
import adn.utilities.Role;

@Component
@Genetized(modelGene = AdminModel.class)
public class AdminModelProducer implements AuthenticationBasedModelProducer<AdminModel, Admin> {

	@Override
	@SecuredFor(role = Role.ADMIN)
	public AdminModel produceForAdminAuthentication(Admin entity, AdminModel model) {
		// TODO Auto-generated method stub
		model.setContractDate(entity.getContractDate());

		return model;
	}

}
