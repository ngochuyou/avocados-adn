/**
 * 
 */
package adn.controller;

import java.util.Optional;

import org.hibernate.FlushMode;
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
import adn.model.AbstractModel;
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
	public static final String ACCESS_DENIED = "ACCESS DENIDED";
	protected static final String EXISTED = "RESOURCE IS ALREADY EXSITED";

	protected void openSession() {
		openSession(FlushMode.MANUAL);
	}

	protected void openSession(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(Optional.ofNullable(mode).orElse(FlushMode.MANUAL));
	}

	@SuppressWarnings("unchecked")
	protected void currentSession(HandledConsumer<Session, Exception>... fncs) {
		try {
			for (HandledConsumer<Session, Exception> fnc : fncs) {
				fnc.accept(sessionFactory.getCurrentSession());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected <T extends Entity, M extends Model> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, modelsDescriptor.instantiate(entityClass));
	}

	protected <T extends Entity, M extends AbstractModel> M produce(T entity, Class<M> modelClass) {
		return authenticationBasedProducerProvider.produce(entity, modelClass, ContextProvider.getPrincipalRole());
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity, M extends AbstractModel> M produce(T entity) {
		return (M) authenticationBasedProducerProvider.produce(entity,
				modelsDescriptor.getModelClass(entity.getClass()), ContextProvider.getPrincipalRole());
	}

	protected <T> ResponseEntity<?> unauthorize(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	protected <T> ResponseEntity<?> sendNotFound(T body) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	protected <T> ResponseEntity<?> send(T instance, String messageIfNotFound) {
		return instance == null ? sendNotFound(messageIfNotFound) : ResponseEntity.ok(instance);
	}

	protected <T extends Entity> ResponseEntity<?> send(T instance, String messageIfNotFound) {
		return instance == null ? sendNotFound(messageIfNotFound) : ResponseEntity.ok(produce(instance));
	}

	@SuppressWarnings("unchecked")
	protected <T extends Entity> ResponseEntity<?> send(DatabaseInteractionResult<T> result) {
		currentSession(ss -> {
			if (result.isOk()) {
				ss.flush();
				return;
			}

			ss.clear();
		});

		return result.isOk() ? ResponseEntity.ok(produce(result.getInstance()))
				: ResponseEntity.status(result.getStatus()).body(result.getMessages());
	}

}
