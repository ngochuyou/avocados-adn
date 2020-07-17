/**
 * 
 */
package adn.service.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import adn.application.Constants;
import adn.model.entities.Account;
import adn.service.ApplicationService;
import adn.service.GenericService;
import adn.utilities.Gender;
import adn.utilities.Role;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@GenericService(target = Account.class)
public class AccountService implements ApplicationService<Account> {

	@Override
	public Account doProcedure(Account model) {
		// TODO Auto-generated method stub
		model.setId(Strings.removeSpaces(model.getId()));
		model.setFirstName(Strings.normalizeString(model.getFirstName()));
		model.setLastName(Strings.normalizeString(model.getLastName()));
		model.setPhoto(Strings.isEmpty(model.getPhoto()) ? Constants.DEFAULT_IMAGE_NAME : model.getPhoto());

		return model;
	}

	@Override
	public Account doInsertionProcedure(Account model) {
		// TODO Auto-generated method stub
		model.setRole(model.getRole() == null ? Role.ANONYMOUS : model.getRole());
		model.setGender(model.getGender() == null ? Gender.UNKNOWN : model.getGender());
		model.setPassword(new BCryptPasswordEncoder().encode(model.getPassword()));

		return model;
	}

	@Override
	public Account doUpdateProcedure(Account model) {
		// TODO Auto-generated method stub
		if (model.getPassword() != null) {
			model.setPassword(new BCryptPasswordEncoder().encode(model.getPassword()));
		}

		return model;
	}

}
