package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Admin;
import adn.model.models.AdminModel;

@Component("adminExtractor")
@Genetized(entityGene = Admin.class)
public class AdminExtractor extends AccountExtractor<Admin, AdminModel> {

	@Override
	public Admin extract(AdminModel model, Admin account) {
		// TODO Auto-generated method stub
		account = super.extract(model, account);
		account.setContractDate(model.getContractDate());

		return account;
	}

	@Override
	public <E extends Admin> E merge(Admin model, E target) {
		// TODO Auto-generated method stub
		target = super.merge(model, target);
		target.setContractDate(model.getContractDate());

		return target;
	}

}
