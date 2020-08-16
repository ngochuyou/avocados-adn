package adn.model.factory.extraction;

import adn.model.entities.Personnel;
import adn.model.factory.EntityExtractor;
import adn.model.models.PersonnelModel;

public class PersonnelExtractor implements EntityExtractor<Personnel, PersonnelModel> {

	@Override
	public Personnel extract(PersonnelModel model, Personnel entity) {
		// TODO Auto-generated method stub
		entity.setCreatedBy(model.getCreatedBy());

		return entity;
	}

}
