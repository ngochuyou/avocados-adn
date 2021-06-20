/**
 * 
 */
package adn.service.entity;

import static adn.helpers.StringHelper.get;
import static adn.helpers.StringHelper.normalizeString;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import adn.helpers.Gender;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Account;
import adn.service.Role;
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

	@Override
	public T defaultBuild(final T model) {
		T persistence = super.defaultBuild(model);
		// we do not set the id field since it's already been required for the
		// instantiating process
		persistence.setEmail(model.getEmail().trim());
		persistence.setPhone(model.getPhone().trim());
		persistence
				.setFirstName(get(normalizeString(model.getFirstName())).orElse(AccountService.UNKNOWN_USER_FIRSTNAME));
		persistence.setLastName(get(normalizeString(model.getLastName())).orElse(AccountService.UNKNOWN_USER_LASTNAME));
		persistence.setPhoto(get(model.getPhoto()).orElse(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME));
		persistence.setGender(Optional.ofNullable(model.getGender()).orElse(Gender.UNKNOWN));

		return persistence;
	}

	@Override
	public T insertionBuild(final T model) {
		T persistence = super.insertionBuild(model);

		persistence.setRole(Optional.ofNullable(model.getRole()).orElse(Role.ANONYMOUS));
		persistence.setPassword(model.getPassword() == null ? "" : passwordEncoder.encode(model.getPassword()));

		return persistence;
	}

	/**
	 * Do not update account role
	 */
	@Override
	public T updateBuild(final T model) {
		T persistence = super.updateBuild(model);
		// leave out model's password if there's no need of password editing
		if (StringHelper.hasLength(model.getPassword())) {
			persistence.setPassword(passwordEncoder.encode(model.getPassword()));
		}

		return persistence;
	}

}
