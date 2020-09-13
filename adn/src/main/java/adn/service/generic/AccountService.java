/**
 * 
 */
package adn.service.generic;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import adn.application.Constants;
import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.service.GenericService;
import adn.utilities.Gender;
import adn.utilities.Role;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(entityGene = Account.class)
public class AccountService implements GenericService<Account> {

	private final String UNKNOWN_USER_FIRSTNAME = "ADN";

	private final String UNKNOWN_USER_LASTNAME = "USER";

	private final Map<Role, Class<? extends Account>> roleClassMap = Map.of(Role.ADMIN, Admin.class, Role.CUSTOMER,
			Customer.class, Role.PERSONNEL, Personnel.class);

	@Override
	public Account executeDefaultProcedure(Account model) {
		// TODO Auto-generated method stub
		model.setId(Strings.removeSpaces(model.getId()));
		model.setFirstName(Strings.isEmpty(model.getFirstName()) ? UNKNOWN_USER_FIRSTNAME
				: Strings.normalizeString(model.getFirstName()));
		model.setLastName(Strings.isEmpty(model.getLastName()) ? UNKNOWN_USER_LASTNAME
				: Strings.normalizeString(model.getLastName()));
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
		if (!Strings.isEmpty(model.getPassword())) {
			model.setPassword(new BCryptPasswordEncoder().encode(model.getPassword()));
		}

		return model;
	}

	@SuppressWarnings("unchecked")
	public <T extends Account> Class<T> getClassFromRole(Role role) {

		return (Class<T>) this.roleClassMap.get(role);
	}

	public <T extends Account> Role getRoleFromClass(Class<T> clazz) {

		return this.roleClassMap.keySet().stream().filter(key -> this.roleClassMap.get(key).equals(clazz)).findFirst()
				.orElse(null);
	}

}
