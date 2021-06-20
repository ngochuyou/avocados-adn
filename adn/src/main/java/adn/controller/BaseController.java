/**
 * 
 */
package adn.controller;

import java.util.Optional;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.context.ContextProvider;
import adn.dao.BaseDAO;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.model.ModelsDescriptor;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractorProvider;
import adn.model.factory.extraction.DelegateEntityExtractorProvider;
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
	protected AuthenticationBasedProducerProvider authenticationBasedProducerProvider;

	@Autowired
	@Qualifier(DelegateEntityExtractorProvider.NAME)
	protected EntityExtractorProvider extractorProvider;

	@Autowired
	protected BaseDAO dao;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ObjectMapper mapper;

	protected static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";

	protected static final String UPLOAD_FAILURE = "Unable to upload file";
	protected static final String NOT_FOUND = "NOT FOUND";
	protected static final String LOCKED = "RESOURCE WAS DEACTIVATED";
	protected static final String INVALID_MODEL = "INVALID MODEL";
	protected static final String ACCESS_DENIED = "ACCESS DENIDED";
	protected static final String EXISTED = "RESOURCE IS ALREADY EXSITED";

	protected void openSession() {
		openSession(null);
	}

	protected void openSession(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(Optional.ofNullable(mode).orElse(FlushMode.MANUAL));
	}

	@SuppressWarnings("unchecked")
	protected <E extends Exception> void currentSession(HandledConsumer<Session, E>... fncs) throws Exception {
		try {
			for (HandledConsumer<Session, E> fnc : fncs) {
				fnc.accept(sessionFactory.getCurrentSession());
			}
		} catch (HibernateException he) {
			he.printStackTrace();
		}
	}

	protected <T extends Entity, M extends Model> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, modelsDescriptor.instantiate(entityClass));
	}

	protected <T extends Entity, M extends Model> M produce(T entity, Class<M> modelClass) {
		return authenticationBasedProducerProvider.produce(entity, modelClass, ContextProvider.getPrincipalRole());
	}

}
