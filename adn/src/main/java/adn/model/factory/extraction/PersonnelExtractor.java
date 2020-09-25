package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Personnel;
import adn.model.factory.GenericEntityExtractor;
import adn.model.models.PersonnelModel;

@Genetized(entityGene = Personnel.class)
public class PersonnelExtractor implements GenericEntityExtractor<Personnel, PersonnelModel> {

	@Override
	public Personnel extract(PersonnelModel model, Personnel entity) {
		// TODO Auto-generated method stub
		entity.setCreatedBy(model.getCreatedBy());

		return entity;
	}

	@Override
	public <E extends Personnel> E map(Personnel model, E target) {
		// TODO Auto-generated method stub
		target.setCreatedBy(model.getCreatedBy());

		return target;
	}

}
