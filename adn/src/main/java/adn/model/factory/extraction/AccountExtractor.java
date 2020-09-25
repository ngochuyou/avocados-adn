package adn.model.factory.extraction;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.factory.GenericEntityExtractor;
import adn.model.models.AccountModel;
import adn.utilities.Gender;
import adn.utilities.Role;

@Component
@Genetized(entityGene = Account.class)
public class AccountExtractor<A extends Account, AM extends AccountModel> implements GenericEntityExtractor<A, AM> {

	protected final String nullModel = "extraction: model and target can not be null";

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

	@Override
	public <E extends A> E map(A model, E target) {
		// TODO Auto-generated method stub
		Assert.notNull(model, nullModel);
		Assert.notNull(target, nullModel);

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
