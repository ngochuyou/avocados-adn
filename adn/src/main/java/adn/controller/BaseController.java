/**
 * 
 */
package adn.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.DefaultAuthenticationBasedModelFactory;
import adn.model.factory.DelegateEntityExtractorProvider;
import adn.model.factory.EntityExtractorProvider;
import adn.model.models.Model;
import adn.service.internal.CRUDService;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class BaseController {

	@Autowired
	protected ModelContextProvider modelsDescriptor;

	@Autowired
	@Qualifier(DefaultAuthenticationBasedModelFactory.NAME)
	protected AuthenticationBasedModelFactory authenticationBasedModelFactory;

	@Autowired
	@Qualifier(DelegateEntityExtractorProvider.NAME)
	protected EntityExtractorProvider extractorProvider;

	@Autowired
	protected CRUDService crudService;

	@Autowired
	protected Repository baseRepository;

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

	protected void setMode() {
		setMode(FlushMode.MANUAL);
	}

	protected void setMode(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(Optional.ofNullable(mode).orElse(FlushMode.MANUAL));
	}

	@SuppressWarnings("unchecked")
	protected void currentSession(HandledConsumer<Session, Exception>... fncs) {
		try {
			for (HandledConsumer<Session, Exception> fnc : fncs) {
				fnc.accept(sessionFactory.getCurrentSession());
			}
		} catch (Exception any) {
			any.printStackTrace();
		}
	}

	protected <T extends Entity, M extends Model> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, modelsDescriptor.instantiate(entityClass));
	}

	protected <T extends AbstractModel, E extends T> Map<String, Object> produce(E entity, Class<E> entityClass) {
		return authenticationBasedModelFactory.produce(entityClass, entity, ContextProvider.getPrincipalRole());
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractModel, E extends T> Map<String, Object> produce(E entity) {
		return authenticationBasedModelFactory.produce((Class<E>) entity.getClass(), entity,
				ContextProvider.getPrincipalRole());
	}

	protected <T extends AbstractModel, E extends T> Map<String, Object> produce(E entity, Class<E> entityClass,
			Role role) {
		return authenticationBasedModelFactory.produce(entityClass, entity, role);
	}

	protected <T> ResponseEntity<?> unauthorize(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	protected <T> ResponseEntity<?> sendNotFound(T body) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	protected <T> ResponseEntity<?> send(T instance, String messageIfNull) {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(instance);
	}

	protected <T extends AbstractModel> ResponseEntity<?> send(List<T> instances) {
		return ResponseEntity.ok(instances.stream().map(this::produce).collect(Collectors.toList()));
	}

	protected <T extends AbstractModel> ResponseEntity<?> send(T instance, String messageIfNull) {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(produce(instance));
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
