/**
 * 
 */
package adn.factory.generic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.factory.EMFactory;
import adn.factory.EMProductionException;
import adn.factory.Factory;
import adn.model.entities.Personnel;
import adn.model.models.PersonnelModel;

/**
 * @author Ngoc Huy
 *
 */
@Component
@EMFactory(entityClass = Personnel.class, modelClass = PersonnelModel.class)
public class PersonnelFactory implements Factory<Personnel, PersonnelModel> {

	@SuppressWarnings("rawtypes")
	@Autowired
	private AccountFactory accountFactory;

	@SuppressWarnings("unchecked")
	@Override
	public Personnel produceEntity(PersonnelModel model, Class<Personnel> clazz) throws EMProductionException {
		// TODO Auto-generated method stub
		Personnel personnel = (Personnel) accountFactory.produceEntity(model, clazz);

		personnel.setCreatedBy(model.getCreatedBy());

		return serviceManager.getService(clazz).executeDefaultProcedure(personnel);
	}

	@SuppressWarnings("unchecked")
	@Override
	public PersonnelModel produceModel(Personnel entity, Class<PersonnelModel> clazz) {
		// TODO Auto-generated method stub
		PersonnelModel model = (PersonnelModel) accountFactory.produceModel(entity, clazz);

		model.setCreatedBy(model.getCreatedBy());

		return model;
	}

}
