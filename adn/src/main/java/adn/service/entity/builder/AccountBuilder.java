/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.get;
import static adn.helpers.StringHelper.normalizeString;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Account;
import adn.model.entities.Gender;
import adn.service.services.AccountService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Account.class)
public class AccountBuilder<T extends Account> extends AbstractEntityBuilder<T> {

	@Autowired
	protected PasswordEncoder passwordEncoder;

	protected <E extends Account> E mandatoryBuild(E target, E model) {
		// we assumes identifier will always be set before
		target.setEmail(model.getEmail().trim());
		target.setPhone(model.getPhone().trim());
		target.setFirstName(get(normalizeString(model.getFirstName())).orElse(AccountService.UNKNOWN_USER_FIRSTNAME));
		target.setLastName(get(normalizeString(model.getLastName())).orElse(AccountService.UNKNOWN_USER_LASTNAME));
		target.setGender(Optional.ofNullable(model.getGender()).orElse(Gender.UNKNOWN));
		target.setRole(model.getRole());
		target.setActive(model.isActive());
		target.setPhoto(get(model.getPhoto()).orElse(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME));

		return target;
	}

	@Override
	public T insertionBuild(T entity) {
		super.insertionBuild(entity);

		mandatoryBuild(entity, entity);
		entity.setPassword(entity.getPassword() == null ? "" : passwordEncoder.encode(entity.getPassword()));

		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T updateBuild(T entity) {
		super.updateBuild(entity);
		// assumes entity is persistent
		T persistence = (T) loadPersistence(entity.getClass(), entity.getId());

		mandatoryBuild(persistence, entity);
		// leave out model's password if there's no need of password editing
		if (StringHelper.hasLength(entity.getPassword())) {
			persistence.setPassword(passwordEncoder.encode(entity.getPassword()));
		}

		return persistence;
	}

	@Override
	public T deactivationBuild(T entity) {
		entity.setDeactivatedDate(LocalDate.now());

		return entity;
	}

}
