/**
 * 
 */
package adn.service.generic;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.application.Constants;
import adn.application.context.ContextProvider;
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
			Customer.class, Role.PERSONNEL, Personnel.class, Role.ANONYMOUS, Account.class);

	@Override
	public Account executeDefaultProcedure(Account model) {
		// TODO Auto-generated method stub
		model.setId(Strings.removeSpaces(model.getId()));
		model.setFirstName(Strings.isEmpty(model.getFirstName()) ? UNKNOWN_USER_FIRSTNAME
				: Strings.normalizeString(model.getFirstName()));
		model.setLastName(Strings.isEmpty(model.getLastName()) ? UNKNOWN_USER_LASTNAME
				: Strings.normalizeString(model.getLastName()));
		model.setPhoto(Strings.isEmpty(model.getPhoto()) ? Constants.DEFAULT_USER_PHOTO_NAME : model.getPhoto());

		return model;
	}

	@Override
	public Account executeInsertionProcedure(Account account) {
		// TODO Auto-generated method stub
		account.setRole(account.getRole() == null ? Role.ANONYMOUS : account.getRole());
		account.setGender(account.getGender() == null ? Gender.UNKNOWN : account.getGender());
		account.setPassword(
				account.getPassword() == null ? "" : new BCryptPasswordEncoder().encode(account.getPassword()));

		return account;
	}

	@Transactional(readOnly = true)
	@Override
	public Account executeUpdateProcedure(Account account) {
		// TODO Auto-generated method stub
		Session session = ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();

		session.evict(account);

		Account persisted = session.get(Account.class, account.getId());
		
		persisted.setEmail(account.getEmail());
		persisted.setPhone(account.getPhone());
		persisted.setFirstName(account.getPhone());
		persisted.setLastName(account.getLastName());
		persisted.setPhoto(account.getPhoto());
		persisted.setGender(account.getGender());

		return persisted;
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
