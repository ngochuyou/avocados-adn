package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Admin;
import adn.model.factory.GenericEntityExtractor;
import adn.model.models.AdminModel;

@Genetized(entityGene = Admin.class)
public class AdminExtractor implements GenericEntityExtractor<Admin, AdminModel> {

	@Override
	public Admin extract(AdminModel model, Admin entity) {
		// TODO Auto-generated method stub
		entity.setContractDate(model.getContractDate());

		return entity;
	}

	@Override
	public <E extends Admin> E map(Admin model, E target) {
		// TODO Auto-generated method stub
		target.setContractDate(model.getContractDate());

		return target;
	}

}
