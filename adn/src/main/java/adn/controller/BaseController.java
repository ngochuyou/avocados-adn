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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.context.ContextProvider;
import adn.dao.Repository;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.model.DatabaseInteractionResult;
import adn.model.ModelsDescriptor;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractorProvider;
import adn.model.factory.extraction.DelegateEntityExtractorProvider;
import adn.model.factory.production.security.AuthenticationBasedProducerProvider;
import adn.model.models.Model;
import adn.service.internal.CRUDService;

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
	protected CRUDService<Entity> crudService;

	@Autowired
	protected Repository<Entity> baseRepository;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ObjectMapper objectMapper;

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

	protected <T> ResponseEntity<?> unauthorize(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	protected <T> ResponseEntity<?> sendNotFound(T body) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	protected <T> ResponseEntity<?> sendFromDatabaseInteractionResult(DatabaseInteractionResult<T> result) {
		return ResponseEntity.status(result.getStatus()).body(result.getMessages());
	}

}
