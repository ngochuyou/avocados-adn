/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.annotation.MultipartConfig;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.builders.ModelContextProvider;
import adn.dao.generic.GenericRepository;
import adn.helpers.CollectionHelper;
import adn.model.DomainEntity;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.dynamicmap.SourceMetadataFactory;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.factory.extraction.PojoEntityExtractorProvider;
import adn.service.internal.GenericCRUDService;
import adn.service.internal.GenericService;
import adn.service.internal.Service.Status;

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
	protected DynamicMapModelProducerFactory dynamicMapModelFactory;

	@Autowired
	protected PojoEntityExtractorProvider extractorProvider;

	@Autowired
	protected GenericCRUDService crudService;

	@Autowired
	protected GenericRepository genericRepository;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected GenericService genericService;

	public static final long MAXIMUM_FILE_SIZE = 30 * 1024 * 1024;

	protected static final String HEAD = "ROLE_HEAD";
	protected static final String PERSONNEL = "ROLE_PERSONNEL";
	protected static final String CUSTOMER = "ROLE_CUSTOMER";
	// for @PreAuthorize
	protected static final String HEAD_OR_PERSONNEL = "ROLE_HEAD OR ROLE_PERSONNEL";

	protected <T extends DomainEntity, M extends DomainEntity> T extract(Class<T> entityClass, M model, T entity) {
		return extractorProvider.getExtractor(entityClass).extract(model, entity);
	}

	protected <T extends DomainEntity> Map<String, Object> produce(T entity, Class<T> entityClass)
			throws UnauthorizedCredential {
		return produce(entity, entityClass, getPrincipalCredential());
	}

	protected <T extends DomainEntity> Map<String, Object> produce(T entity, Class<T> entityClass,
			Credential credential) throws UnauthorizedCredential {
		return dynamicMapModelFactory.producePojo(entity,
				SourceMetadataFactory.unknown(entityClass,
						CollectionHelper.list(modelContext.getMetadata(entityClass).getNonLazyPropertyNames())),
				credential);
	}

	protected <T> ResponseEntity<?> unauthorize() {
		return unauthorize(Map.of(Common.MESSAGE, Common.ACCESS_DENIED));
	}

	protected <T> ResponseEntity<?> unauthorize(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	protected <T> ResponseEntity<?> notFound() {
		return notFound(Common.message(Common.notfound()));
	}

	protected <T> ResponseEntity<?> notFound(T body) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	protected <T> ResponseEntity<?> bad(T body) {
		return ResponseEntity.badRequest().body(body);
	}

	protected <T> ResponseEntity<?> fails(T instance) {
		return ResponseEntity.internalServerError().body(instance);
	}

	protected <T> ResponseEntity<?> conflict(T instance) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(instance);
	}

	protected <T, B> ResponseEntity<?> send(T instance, B ifNull) {
		return instance == null ? notFound(ifNull) : ResponseEntity.ok(instance);
	}

	protected <T extends DomainEntity, E extends T> ResponseEntity<?> send(E instance, Class<E> type,
			String messageIfNull) throws UnauthorizedCredential {
		return send(instance, type, messageIfNull, getPrincipalCredential());
	}

	protected <T extends DomainEntity, E extends T> ResponseEntity<?> send(E instance, Class<E> type,
			String messageIfNull, Credential credential) throws UnauthorizedCredential {
		return instance == null ? notFound(messageIfNull) : ResponseEntity.ok(produce(instance, type, credential));
	}

	@SuppressWarnings("unchecked")
	protected <T extends DomainEntity> ResponseEntity<?> send(Result<T> result) throws Exception {
		if (result.isOk()) {
			T instance = result.getInstance();

			try {
				return ResponseEntity.ok(produce(instance, (Class<T>) instance.getClass()));
			} catch (UnauthorizedCredential e) {
				return fails(e.getMessage());
			}
		}

		if (result.getStatus() == Status.BAD) {
			return bad(result.getMessages());
		}

		return fails(result.getMessages());
	}

	protected <T> ResponseEntity<?> makeStaleWhileRevalidate(T body, long maxAge, TimeUnit maxAgeDurationUnit,
			long revalidateDuration, TimeUnit revalidateDurationUnit) {
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(maxAge, maxAgeDurationUnit)
				.staleWhileRevalidate(revalidateDuration, maxAgeDurationUnit)).body(body);
	}

}
