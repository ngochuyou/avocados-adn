/**
 * 
 */
package adn.service.generic;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import adn.application.Constants;
import adn.model.Genetized;
import adn.model.entities.Account;
import adn.service.GenericService;
import adn.utilities.Gender;
import adn.utilities.Role;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(gene = Account.class)
public class AccountService implements GenericService<Account> {

	@Override
	public Account executeDefaultProcedure(Account model) {
		// TODO Auto-generated method stub
		model.setId(Strings.removeSpaces(model.getId()));
		model.setFirstName(Strings.normalizeString(model.getFirstName()));
		model.setLastName(Strings.normalizeString(model.getLastName()));
		model.setPhoto(Strings.isEmpty(model.getPhoto()) ? Constants.DEFAULT_IMAGE_NAME : model.getPhoto());

		return model;
	}

	@Override
	public Account executeInsertionProcedure(Account model) {
		// TODO Auto-generated method stub
		model.setRole(model.getRole() == null ? Role.ANONYMOUS : model.getRole());
		model.setGender(model.getGender() == null ? Gender.UNKNOWN : model.getGender());
		model.setPassword(model.getPassword() == null ? "" : new BCryptPasswordEncoder().encode(model.getPassword()));

		return model;
	}

	@Override
	public Account executeUpdateProcedure(Account model) {
		// TODO Auto-generated method stub
		if (model.getPassword() != null) {
			model.setPassword(new BCryptPasswordEncoder().encode(model.getPassword()));
		}

		return model;
	}

}
