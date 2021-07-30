/**
 * 
 */
package adn.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.annotation.MultipartConfig;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.context.ContextProvider;
import adn.dao.DatabaseInteractionResult;
import adn.dao.Repository;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.model.DomainEntity;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.pojo.extraction.EntityExtractorProvider;
import adn.service.internal.CRUDService;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
@MultipartConfig(maxFileSize = BaseController.MAXIMUM_FILE_SIZE)
public class BaseController {

	@Autowired
	protected ModelContextProvider modelContext;

	@Autowired
	protected AuthenticationBasedModelFactory authenticationBasedModelFactory;

	@Autowired
	protected AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory;

	@Autowired
	protected EntityExtractorProvider extractorProvider;

	@Autowired
	protected CRUDService crudService;

	@Autowired
	protected Repository baseRepository;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ObjectMapper objectMapper;

	public static final long MAXIMUM_FILE_SIZE = 30 * 1024 * 1024;

	protected static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";

	protected static final String LOCKED = "RESOURCE WAS DEACTIVATED";
	protected static final String INVALID_MODEL = "INVALID MODEL";
	public static final String ACCESS_DENIED = "ACCESS DENIDED";
	protected static final String EXISTED = "RESOURCE IS ALREADY EXSITED";

	protected void setSessionMode() {
		setSessionMode(FlushMode.MANUAL);
	}

	protected void setSessionMode(FlushMode mode) {
		sessionFactory.getCurrentSession().setHibernateFlushMode(Optional.ofNullable(mode).orElse(FlushMode.MANUAL));
	}

	protected void currentSession(HandledConsumer<Session, Exception> fnc) {
		try {
			fnc.accept(sessionFactory.getCurrentSession());
		} catch (Exception any) {
			any.printStackTrace();
		}
	}

	protected <T extends DomainEntity, M extends DomainEntity> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, modelContext.instantiate(entityClass));
	}

	protected <T extends DomainEntity, E extends T> Map<String, Object> produce(E entity, Class<E> entityClass) {
		return produce(entity, entityClass, ContextProvider.getPrincipalRole());
	}

	@SuppressWarnings("unchecked")
	protected <T extends DomainEntity, E extends T> Map<String, Object> produce(E entity) {
		return produce(entity, (Class<E>) entity.getClass(), ContextProvider.getPrincipalRole());
	}

	protected <T extends DomainEntity, E extends T> Map<String, Object> produce(E entity, Class<E> entityClass,
			Role role) {
		return authenticationBasedModelFactory.produce(entityClass, entity, role);
	}

	protected <T> ResponseEntity<?> unauthorize(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	protected <T> ResponseEntity<?> sendNotFound(T body) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	protected <T> ResponseEntity<?> sendBadRequest(T body) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	protected <T> ResponseEntity<?> send(T instance, String messageIfNull) {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(instance);
	}

	protected <T extends DomainEntity> ResponseEntity<?> send(List<T> instances) {
		return ResponseEntity.ok(instances.stream().map(this::produce).collect(Collectors.toList()));
	}

	protected <T extends DomainEntity> ResponseEntity<?> send(T instance, String messageIfNull) {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(produce(instance));
	}

	protected <T extends DomainEntity, E extends T> ResponseEntity<?> send(E instance, Class<E> type,
			String messageIfNull) {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(produce(instance, type));
	}

	protected <T> ResponseEntity<?> fails(T instance) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(instance);
	}

	protected <T extends Entity> ResponseEntity<?> send(DatabaseInteractionResult<T> result) {
		return result.isOk() ? ResponseEntity.ok(produce(result.getInstance()))
				: ResponseEntity.status(result.getStatus()).body(result.getMessages());
	}

	protected <T> ResponseEntity<?> cache(T body, long age, TimeUnit unit) {
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(age, unit)).body(body);
	}

	protected <T> ResponseEntity<?> makeStaleWhileRevalidate(T body, long maxAge, TimeUnit maxAgeDurationUnit,
			long revalidateDuration, TimeUnit revalidateDurationUnit) {
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(maxAge, maxAgeDurationUnit)
				.staleWhileRevalidate(revalidateDuration, maxAgeDurationUnit)).body(body);
	}

}
