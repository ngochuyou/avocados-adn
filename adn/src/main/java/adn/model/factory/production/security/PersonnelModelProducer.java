package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Personnel;
import adn.model.models.PersonnelModel;
import adn.security.SecuredFor;
import adn.service.services.Role;

@Component
@Genetized(modelGene = PersonnelModel.class)
public class PersonnelModelProducer implements AuthenticationBasedModelProducer<Personnel, PersonnelModel> {

	@Override
	@SecuredFor(role = Role.ADMIN)
	public PersonnelModel produceForAdminAuthentication(Personnel entity, PersonnelModel model) {
		// TODO Auto-generated method stub
		model.setCreatedBy(entity.getCreatedBy());

		return model;
	}

	@Override
	@SecuredFor(role = Role.PERSONNEL)
	public PersonnelModel produceForPersonnelAuthentication(Personnel entity, PersonnelModel model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}

	@Override
	@SecuredFor(role = Role.CUSTOMER)
	public PersonnelModel produceForCustomerAuthentication(Personnel entity, PersonnelModel model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}

}
