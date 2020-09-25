/**
 * 
 */
package adn.dao.generic;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import adn.application.Constants;
import adn.dao.BaseDAO;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.factory.extraction.AccountExtractor;
import adn.service.services.AccountService;
import adn.utilities.Gender;
import adn.utilities.Role;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Account.class)
public class AccountDAO extends BaseDAO implements GenericDAO<Account> {

	@SuppressWarnings("rawtypes")
	@Autowired
	private AccountExtractor extractor;

	@Override
	public Account defaultBuild(Account model) {
		// TODO Auto-generated method stub
		model.setId(Strings.removeSpaces(model.getId()));
		model.setFirstName(Strings.isEmpty(model.getFirstName()) ? AccountService.UNKNOWN_USER_FIRSTNAME
				: Strings.normalizeString(model.getFirstName()));
		model.setLastName(Strings.isEmpty(model.getLastName()) ? AccountService.UNKNOWN_USER_LASTNAME
				: Strings.normalizeString(model.getLastName()));
		model.setPhoto(Strings.isEmpty(model.getPhoto()) ? Constants.DEFAULT_USER_PHOTO_NAME : model.getPhoto());

		return model;
	}

	@Override
	public Account insertBuild(Account account) {
		// TODO Auto-generated method stub
		account.setRole(account.getRole() == null ? Role.ANONYMOUS : account.getRole());
		account.setGender(account.getGender() == null ? Gender.UNKNOWN : account.getGender());
		account.setPassword(
				account.getPassword() == null ? "" : new BCryptPasswordEncoder().encode(account.getPassword()));

		return account;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public Account updateBuild(Account model) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		Account persisted = session.get(Account.class, model.getId());

		persisted.setEmail(model.getEmail());
		persisted.setPhone(model.getPhone());
		persisted.setFirstName(model.getPhone());
		persisted.setLastName(model.getLastName());
		persisted.setPhoto(model.getPhoto());
		persisted.setGender(model.getGender());

		if (!persisted.getRole().equals(model.getRole())) {
			persisted.setRole(model.getRole());
			persisted = extractor.map(persisted, model);
			updateDType(persisted, persisted.getClass());
			
			return persisted;
		}

		return persisted;
	}

}
