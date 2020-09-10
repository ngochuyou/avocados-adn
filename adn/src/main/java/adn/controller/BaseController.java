/**
 * 
 */
package adn.controller;

import javax.transaction.Transactional;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.application.managers.AuthenticationBasedEMFactory;
import adn.application.managers.ModelManager;
import adn.dao.BaseDAO;
import adn.model.entities.Entity;
import adn.model.models.Model;
import adn.security.ApplicationUserDetails;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Transactional
@Component
public class BaseController {

	@Autowired
	protected ModelManager modelManager;

	@Autowired
	protected AuthenticationBasedEMFactory authBasedEMFactory;

	@Autowired
	protected BaseDAO dao;

	@Autowired
	protected SessionFactory sessionFactory;

	protected final String hasRoleAdmin = "hasRole('ADMIN')";

	protected <T> ResponseEntity<T> handleSuccess(T body) {

		return new ResponseEntity<T>(body, null, HttpStatus.OK);
	}

	protected <T> ResponseEntity<T> handleFailure(T body, int status) {

		return new ResponseEntity<T>(body, HttpStatus.valueOf(status));
	}

	protected void openSession(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(mode != null ? mode : FlushMode.MANUAL);
	}

	protected void openSession() {
		this.openSession(null);
	}

	protected void closeSession(boolean isFlushed) {
		if (isFlushed) {
			sessionFactory.getCurrentSession().flush();

			return;
		}

		sessionFactory.getCurrentSession().close();
	}

	protected <T extends Entity, M extends Model> M produceModel(T entity, Class<M> modelClass) {

		return authBasedEMFactory.produce(entity, modelClass, BaseController.getAuthenticationRole());
	}

	public static Role getAuthenticationRole() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			return Role.ANONYMOUS;
		}

		return ((ApplicationUserDetails) auth.getPrincipal()).getRole();
	}

}
