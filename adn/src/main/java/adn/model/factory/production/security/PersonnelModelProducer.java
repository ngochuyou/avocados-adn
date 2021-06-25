package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Personnel;
import adn.model.models.PersonnelModel;
import adn.security.SecuredFor;
import adn.service.internal.Role;

@Component
@Generic(modelGene = PersonnelModel.class)
public class PersonnelModelProducer implements AuthenticationBasedModelProducer<Personnel, PersonnelModel> {

	@Override
	@SecuredFor(role = Role.ADMIN)
	public PersonnelModel produceForAdminAuthentication(Personnel entity, PersonnelModel model) {
		model.setCreatedBy(entity.getCreatedBy());

		return model;
	}

	@Override
	@SecuredFor(role = Role.PERSONNEL)
	public PersonnelModel produceForPersonnelAuthentication(Personnel entity, PersonnelModel model) {
		return produceForAdminAuthentication(entity, model);
	}

}
