package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Admin;
import adn.model.factory.EntityExtractor;
import adn.model.models.AdminModel;

@Genetized(entityGene = Admin.class)
public class AdminExtractor implements EntityExtractor<Admin, AdminModel> {

	@Override
	public Admin extract(AdminModel model, Admin entity) {
		// TODO Auto-generated method stub
		entity.setContractDate(model.getContractDate());

		return entity;
	}

}