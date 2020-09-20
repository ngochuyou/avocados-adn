/**
 * 
 */
package adn.controller;

import javax.transaction.Transactional;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.Constants;
import adn.dao.BaseDAO;
import adn.model.ModelManager;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractorProvider;
import adn.model.factory.ModelProducerProvider;
import adn.model.models.Model;
import adn.utilities.ClassReflector;

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
	@Qualifier(Constants.defaultModelProducerProdiverName)
	protected ModelProducerProvider producerProvider;

	@Autowired
	@Qualifier(Constants.defaultEntityExtractorProdiverName)
	protected EntityExtractorProvider extractorProvider;

	@Autowired
	protected BaseDAO dao;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ClassReflector reflector;

	@Autowired
	protected ObjectMapper mapper;

	protected final String hasRoleAdmin = "hasRole('ADMIN')";

	protected final String notFound = "NOT FOUND";

	protected final String locked = "RESOURCE IS DEACTIVATED";

	protected final String invalidModel = "INVALID MODEL";

	protected final String accessDenied = "ACCESS DENIDED";

	protected void openSession(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(mode != null ? mode : FlushMode.MANUAL);
	}

	protected void openSession() {
		this.openSession(null);
	}

	protected void clearSession(boolean isFlushed) {
		if (isFlushed) {
			sessionFactory.getCurrentSession().flush();

			return;
		}

		sessionFactory.getCurrentSession().clear();
	}

	protected <T extends Entity, M extends Model> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, reflector.newInstanceOrAbstract(entityClass));
	}

	protected <T extends Entity, M extends Model> M produce(T entity, Class<M> modelClass) {
		return producerProvider.getProducer(modelClass).produce(entity, reflector.newInstanceOrAbstract(modelClass));
	}

}
