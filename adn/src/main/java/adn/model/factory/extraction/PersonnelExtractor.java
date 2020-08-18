package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Personnel;
import adn.model.factory.EntityExtractor;
import adn.model.models.PersonnelModel;

@Genetized(entityGene = Personnel.class)
public class PersonnelExtractor implements EntityExtractor<Personnel, PersonnelModel> {

	@Override
	public Personnel extract(PersonnelModel model, Personnel entity) {
		// TODO Auto-generated method stub
		entity.setCreatedBy(model.getCreatedBy());

		return entity;
	}

}
