/**
 * 
 */
package adn.controller;

import javax.transaction.Transactional;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.application.ContextProvider;
import adn.application.managers.AuthenticationBasedEMFactory;
import adn.application.managers.ModelManager;
import adn.dao.BaseDAO;
import adn.model.entities.Entity;
import adn.model.models.Model;
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

	protected final String notFound = "NOT FOUND";

	protected final String locked = "RESOURCE IS DEACTIVATED";
	
	protected final String invalidModel = "INVALID MODEL";
	
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

		return authBasedEMFactory.produce(entity, modelClass, ContextProvider.getPrincipalRole());
	}

	protected <T extends Entity, M extends Model> M produceModel(T entity, Class<M> modelClass, Role role) {

		return authBasedEMFactory.produce(entity, modelClass, role);
	}

}
