package adn.model.factory.extraction;

import adn.model.entities.Account;
import adn.model.factory.EntityExtractor;
import adn.model.models.AccountModel;
import adn.utilities.Gender;
import adn.utilities.Role;

public class AccountExtractor<A extends Account, AM extends AccountModel> implements EntityExtractor<A, AM> {

	@Override
	public A extract(AM model, A account) {
		// TODO Auto-generated method stub
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

}
