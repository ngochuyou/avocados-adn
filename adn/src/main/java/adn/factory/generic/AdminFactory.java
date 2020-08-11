/**
 * 
 */
package adn.factory.generic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.factory.EMFactory;
import adn.factory.EMProductionException;
import adn.factory.Factory;
import adn.model.entities.Admin;
import adn.model.models.AdminModel;

/**
 * @author Ngoc Huy
 *
 */
@Component
@EMFactory(entityClass = Admin.class, modelClass = AdminModel.class)
public class AdminFactory implements Factory<Admin, AdminModel> {

	@SuppressWarnings({ "rawtypes" })
	@Autowired
	private AccountFactory accountFactory;

	@SuppressWarnings("unchecked")
	@Override
	public Admin produceEntity(AdminModel model, Class<Admin> clazz) throws EMProductionException {
		// TODO Auto-generated method stub
		Admin admin = (Admin) accountFactory.produceEntity(model, Admin.class);

		admin.setContractDate(model.getContractDate());

		return serviceManager.getService(clazz).doProcedure(admin);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AdminModel produceModel(Admin entity, Class<AdminModel> clazz) {
		// TODO Auto-generated method stub
		AdminModel model = (AdminModel) accountFactory.produceModel(entity, clazz);

		model.setContractDate(entity.getContractDate());

		return model;
	}

}
