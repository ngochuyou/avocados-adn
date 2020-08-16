package adn.model.factory.production.security;

import adn.model.entities.Personnel;
import adn.model.models.PersonnelModel;

public class PersonnelModelProducer
		implements ModelProducerBasedOnAuthentication<PersonnelModel, Personnel> {
	
	@Override
	public PersonnelModel produceForAdminAuthentication(Personnel entity, PersonnelModel model) {
		// TODO Auto-generated method stub
		model.setCreatedBy(entity.getCreatedBy());

		return model;
	}
	
	@Override
	public PersonnelModel produceForCustomerAuthentication(Personnel entity, PersonnelModel model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}
	
	@Override
	public PersonnelModel produceForPersonnelAuthentication(Personnel entity, PersonnelModel model) {
		// TODO Auto-generated method stub
		return produceForAdminAuthentication(entity, model);
	}
	
}
