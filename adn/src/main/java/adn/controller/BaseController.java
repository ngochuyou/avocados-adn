/**
 * 
 */
package adn.controller;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.dao.BaseDAO;
import adn.model.ModelsDescriptor;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractorProvider;
import adn.model.factory.production.security.AuthenticationBasedProducerProvider;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class BaseController {

	@Autowired
	protected ModelsDescriptor modelsDescriptor;

	@Autowired
	protected AuthenticationBasedProducerProvider producerProvider;

	@Autowired
	@Qualifier(Constants.DEFAULT_ENTITY_EXTRACTOR_PROVIDER_NAME)
	protected EntityExtractorProvider extractorProvider;

	@Autowired
	protected BaseDAO dao;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ObjectMapper mapper;

	protected final String hasRoleAdmin = "hasRole('ADMIN')";

	protected final String uploadFailure = "Unable to upload file";
	protected final String notFound = "NOT FOUND";
	protected final String locked = "RESOURCE WAS DEACTIVATED";
	protected final String invalidModel = "INVALID MODEL";
	protected final String accessDenied = "ACCESS DENIDED";
	protected final String existed = "RESOURCE IS ALREADY EXSITED";

	protected void openSession(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(mode != null ? mode : FlushMode.MANUAL);
	}

	protected void closeSession(boolean isFlushed) {
		Session session = sessionFactory.getCurrentSession();

		if (isFlushed) {
			session.flush();
		}

		session.clear();
	}

	protected <T extends Entity, M extends Model> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, modelsDescriptor.instantiate(entityClass));
	}

	protected <T extends Entity, M extends Model> M produce(T entity, Class<M> modelClass) {
		return producerProvider.produce(entity, modelClass, ContextProvider.getPrincipalRole());
	}

}
