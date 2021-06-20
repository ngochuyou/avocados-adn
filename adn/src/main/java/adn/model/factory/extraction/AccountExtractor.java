package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.helpers.Gender;
import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.service.services.Role;

@Component("accountExtractor")
@Genetized(entityGene = Account.class)
public class AccountExtractor<A extends Account, AM extends AccountModel> extends ModelEntityExtractor<A, AM> {

	@Override
	public A extract(AM model, A account) {
		// TODO Auto-generated method stub
		account = super.extract(model, account);
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

	@Override
	public <E extends A> E merge(A model, E target) {
		// TODO Auto-generated method stub
		target = super.merge(model, target);
		target.setId(model.getId());
		target.setEmail(model.getEmail());
		target.setPhone(model.getPhone());
		target.setFirstName(model.getFirstName());
		target.setLastName(model.getLastName());
		target.setPhoto(model.getPhoto());
		target.setPassword(model.getPassword());
		target.setRole(model.getRole());
		target.setGender(model.getGender());

		return target;
	}

}
