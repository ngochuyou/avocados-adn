/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.get;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.User;
import adn.model.entities.constants.Gender;
import adn.model.entities.metadata._User;
import adn.service.internal.Role;
import adn.service.services.UserService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = User.class)
public class UserBuilder<T extends User> extends AbstractPermanentEntityBuilder<T> {

	private static final String EMPTY_PASSWORD = "";

	@Autowired
	private PasswordEncoder passwordEncoder;

	protected <E extends T> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);
		// we assumes identifier will always be set before
		target.setEmail(get(model.getEmail()).map(email -> email.trim()).orElse(null));
		target.setPhone(normalizeString(model.getPhone()));
		target.setAddress(normalizeString(model.getAddress()));
		target.setLastName(get(normalizeString(model.getLastName())).orElse(UserService.UNKNOWN_USER_LASTNAME));
		target.setFirstName(get(normalizeString(model.getFirstName())).orElse(UserService.UNKNOWN_USER_FIRSTNAME));
		target.setGender(Optional.ofNullable(model.getGender()).orElse(Gender.UNKNOWN));
		target.setRole(model.getRole());
		target.setPhoto(get(model.getPhoto()).orElse(UserService.DEFAULT_ACCOUNT_PHOTO_NAME));

		return target;
	}

	@Override
	public <E extends T> E buildInsertion(Serializable id, E model, Session session) {
		model = super.buildInsertion(id, model, session);

		if (model.getPassword() == null || model.getPassword().length() < _User.MINIMUM_PASSWORD_LENGTH) {
			model.setPassword(EMPTY_PASSWORD);

			return model;
		}

		model.setPassword(passwordEncoder.encode(model.getPassword()));

		if (model.getRole() == Role.CUSTOMER) {
			model.setLocked(false);
		}

		model.setUpdatedDate(LocalDateTime.now());

		return model;
	}

	@Override
	public <E extends T> E buildUpdate(Serializable id, E model, E persistence, Session session) {
		persistence = super.buildUpdate(id, model, persistence, session);
		// leave out model's password if there's no need of password editing
		if (StringHelper.hasLength(model.getPassword())) {
			persistence.setPassword(passwordEncoder.encode(model.getPassword()));
			persistence.setUpdatedDate(LocalDateTime.now());
		}

		return persistence;
	}

}
