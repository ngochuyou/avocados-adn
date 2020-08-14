/**
 * 
 */
package adn.factory.generic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.factory.EMFactory;
import adn.factory.EMProductionException;
import adn.factory.Factory;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.utilities.Gender;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
@EMFactory(entityClass = Account.class, modelClass = AccountModel.class)
public class AccountFactory<A extends Account, AM extends AccountModel> implements Factory<A, AM> {

	@SuppressWarnings("rawtypes")
	@Autowired
	private EntityFactory abstractFactory;

	@SuppressWarnings("unchecked")
	@Override
	public A produceEntity(AM model, Class<A> clazz) throws EMProductionException {
		// TODO Auto-generated method stub
		A account = (A) abstractFactory.produceEntity(model, Account.class);

		account.setId(model.getUsername());
		account.setEmail(model.getEmail());
		account.setPhone(model.getPhone());
		account.setFirstName(model.getFirstName());
		account.setLastName(model.getLastName());
		account.setPhoto(model.getPhoto());
		account.setPassword(model.getPassword());

		try {
			account.setRole(Role.valueOf(model.getRole()));
		} catch (Exception e) {
			account.setRole(null);
		}

		try {
			account.setGender(Gender.valueOf(model.getGender()));
		} catch (Exception e) {
			account.setGender(null);
		}

		return account;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AM produceModel(A entity, Class<AM> clazz) {
		// TODO Auto-generated method stub
		AM model = (AM) abstractFactory.produceModel(entity, clazz);

		model.setUsername(entity.getId());
		model.setEmail(entity.getEmail());
		model.setPhone(entity.getPhone());
		model.setFirstName(entity.getFirstName());
		model.setLastName(entity.getLastName());
		model.setPhoto(entity.getPhoto());
		model.setPassword(null);
		model.setRole(entity.getRole().toString());
		model.setGender(entity.getGender().toString());

		return model;
	}

}
