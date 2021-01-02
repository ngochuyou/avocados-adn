/**
 * 
 */
package adn.dao.generic;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import adn.application.Constants;
import adn.model.Genetized;
import adn.model.entities.Account;
import adn.model.factory.extraction.AccountExtractor;
import adn.service.services.AccountService;
import adn.utilities.Gender;
import adn.utilities.Role;
import adn.utilities.Strings;
import io.jsonwebtoken.lang.Assert;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Account.class)
public class AccountDAO<T extends Account> extends EntityDAO<T> {

	@SuppressWarnings("rawtypes")
	@Autowired
	@Qualifier("accountExtractor")
	private AccountExtractor extractor;

	@Autowired
	protected AccountService accountService;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public T defaultBuild(T model) {
		// TODO Auto-generated method stub
		model = super.defaultBuild(model);
		model.setId(Strings.removeSpaces(model.getId()));
		model.setFirstName(Strings.isEmpty(model.getFirstName()) ? AccountService.UNKNOWN_USER_FIRSTNAME
				: Strings.normalizeString(model.getFirstName()));
		model.setLastName(Strings.isEmpty(model.getLastName()) ? AccountService.UNKNOWN_USER_LASTNAME
				: Strings.normalizeString(model.getLastName()));
		model.setPhoto(Strings.isEmpty(model.getPhoto()) ? Constants.DEFAULT_USER_PHOTO_NAME : model.getPhoto());

		return model;
	}

	@Override
	public T insertionBuild(T model) {
		// TODO Auto-generated method stub
		model = super.insertionBuild(model);
		model.setRole(model.getRole() == null ? Role.ANONYMOUS : model.getRole());
		model.setGender(model.getGender() == null ? Gender.UNKNOWN : model.getGender());
		model.setPassword(model.getPassword() == null ? "" : new BCryptPasswordEncoder().encode(model.getPassword()));

		return model;
	}

	/**
	 * Account role editing, especially from CUSTOMER to ADMIN or PERSONNEL should
	 * be avoided as much as possible
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T updateBuild(T model) {
		// TODO Auto-generated method stub
		super.updateBuild(model);

		Session session = sessionFactory.getCurrentSession();
		Account persistence = session.load(Account.class, model.getId());

		Assert.notNull(persistence,
				"Cannot find entity with identifier: " + model.getId() + " in the context persistence");
		persistence.setEmail(model.getEmail());
		persistence.setPhone(model.getPhone());
		persistence.setFirstName(model.getPhone());
		persistence.setLastName(model.getLastName());
		persistence.setPhoto(model.getPhoto());
		persistence.setGender(model.getGender());
		// leave out model's password if there's no need for password editing
		if (!Strings.isEmpty(model.getPassword())) {
			persistence.setPassword(passwordEncoder.encode(model.getPassword()));
		}
		// set model's role to null if there's no need for role editing
		if (model.getRole() != null && !persistence.getRole().equals(model.getRole())) {
			// SHOULD BE AVOIDED A.M.A.P
			logger.debug("UPDATING role from " + persistence.getRole() + " to " + model.getRole());
			persistence.setRole(model.getRole());
			// TODO: REMOVING the persisted entity
			session.delete(persistence);
			// creating an entity of the new type since role editing requires entity's class
			// to be modified and then merge the old entity with the new one
			persistence = extractor.map(persistence,
					reflector.newInstanceOrAbstract(accountService.getClassFromRole(model.getRole())));
			// persist the new entity
			session.persist(persistence);
		}

		return model;
	}

}
