/**
 * 
 */
package adn.dao.generic;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import adn.application.Constants;
import adn.helpers.Gender;
import adn.helpers.Role;
import adn.helpers.StringHelper;
import adn.model.Genetized;
import adn.model.ModelManager;
import adn.model.entities.Account;
import adn.model.factory.extraction.AccountExtractor;
import adn.service.services.AccountService;

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

	@Autowired
	protected ModelManager modelManager;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public T defaultBuild(T model) {
		// TODO Auto-generated method stub
		model = super.defaultBuild(model);
		model.setId(StringHelper.removeSpaces(model.getId()));
		model.setFirstName(!StringHelper.hasLength(model.getFirstName()) ? AccountService.UNKNOWN_USER_FIRSTNAME
				: StringHelper.normalizeString(model.getFirstName()));
		model.setLastName(!StringHelper.hasLength(model.getLastName()) ? AccountService.UNKNOWN_USER_LASTNAME
				: StringHelper.normalizeString(model.getLastName()));
		model.setPhoto(!StringHelper.hasLength(model.getPhoto()) ? Constants.DEFAULT_USER_PHOTO_NAME : model.getPhoto());

		return model;
	}

	@Override
	public T insertionBuild(T model) {
		// TODO Auto-generated method stub
		model = super.insertionBuild(model);
		model.setRole(model.getRole() == null ? Role.ANONYMOUS : model.getRole());
		model.setGender(model.getGender() == null ? Gender.UNKNOWN : model.getGender());
		model.setPassword(model.getPassword() == null ? "" : passwordEncoder.encode(model.getPassword()));

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

		Assert.notNull(persistence, "Cannot update null persistence, id: " + model.getId());
		persistence.setEmail(model.getEmail());
		persistence.setPhone(model.getPhone());
		persistence.setFirstName(model.getPhone());
		persistence.setLastName(model.getLastName());
		persistence.setPhoto(model.getPhoto());
		persistence.setGender(model.getGender());
		// leave out model's password if there's no need of password editing
		if (StringHelper.hasLength(model.getPassword())) {
			persistence.setPassword(passwordEncoder.encode(model.getPassword()));
		}
		// set model's role to null if there's no need of role editing
		if (model.getRole() != null && !persistence.getRole().equals(model.getRole())) {
			// SHOULD BE AVOIDED A.M.A.P
			logger.debug("UPDATING role from " + persistence.getRole() + " to " + model.getRole());
			persistence.setRole(model.getRole());
			// TODO: REMOVING the persisted entity
			session.delete(persistence);
			// creating an entity of the new type since role editing requires entity's class
			// to be modified and then merge the old entity with the new one
			persistence = extractor.merge(persistence,
					modelManager.instantiate(accountService.getClassFromRole(model.getRole())));
			// persist the new entity
			session.persist(persistence);
		}

		return model;
	}

}
