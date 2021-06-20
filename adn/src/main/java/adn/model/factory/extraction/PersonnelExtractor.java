package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Personnel;
import adn.model.models.PersonnelModel;

@Component("personnelExtractor")
@Genetized(entityGene = Personnel.class)
public class PersonnelExtractor extends AccountExtractor<Personnel, PersonnelModel> {

	@Override
	public Personnel extract(PersonnelModel model, Personnel personnel) {
		personnel = super.extract(model, personnel);
		personnel.setCreatedBy(model.getCreatedBy());

		return personnel;
	}

	@Override
	public <E extends Personnel> E merge(Personnel model, E target) {
		target = super.merge(model, target);
		target.setCreatedBy(model.getCreatedBy());

		return target;
	}

}
