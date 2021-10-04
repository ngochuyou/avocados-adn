/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.annotation.MultipartConfig;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.context.builders.ModelContextProvider;
import adn.dao.generic.GenericRepository;
import adn.dao.generic.Result;
import adn.helpers.CollectionHelper;
import adn.model.DomainEntity;
import adn.model.entities.Entity;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.dynamicmap.SourceMetadataFactory;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.factory.extraction.PojoEntityExtractorProvider;
import adn.service.internal.GenericCRUDService;
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
	protected GenericRepository baseRepository;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected ObjectMapper objectMapper;

	public static final long MAXIMUM_FILE_SIZE = 30 * 1024 * 1024;

	protected static final String HEAD = "ROLE_HEAD";
	protected static final String PERSONNEL = "ROLE_PERSONNEL";
	// for @PreAuthorize
	protected static final String HEAD_OR_PERSONNEL = "ROLE_HEAD OR ROLE_PERSONNEL";

	protected <T extends DomainEntity, M extends DomainEntity> T extract(M model, Class<T> entityClass) {
		return extractorProvider.getExtractor(entityClass).extract(model, modelContext.instantiate(entityClass));
	}

	protected <T extends DomainEntity, E extends T> Map<String, Object> produce(E entity, Class<E> entityClass)
			throws UnauthorizedCredential {
		return produce(entity, entityClass, getPrincipalCredential());
	}

	@SuppressWarnings("unchecked")
	protected <T extends DomainEntity, E extends T> Map<String, Object> produce(E entity)
			throws UnauthorizedCredential {
		return produce(entity, (Class<E>) entity.getClass(), getPrincipalCredential());
	}

	protected <T extends DomainEntity, E extends T> Map<String, Object> produce(E entity, Class<E> entityClass,
			Credential credential) throws UnauthorizedCredential {
		return dynamicMapModelFactory.producePojo(entity,
				SourceMetadataFactory.unknown(entityClass,
						CollectionHelper.list(modelContext.getMetadata(entityClass).getNonLazyPropertyNames())),
				credential);
	}

	protected <T> ResponseEntity<?> unauthorize(T body) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	protected <T> ResponseEntity<?> sendNotFound(T body) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	protected <T> ResponseEntity<?> sendBad(T body) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	protected <T, B> ResponseEntity<?> send(T instance, B ifNull) {
		return instance == null ? sendNotFound(ifNull) : ResponseEntity.ok(instance);
	}

	protected <T extends DomainEntity> ResponseEntity<?> send(List<T> instances) {
		return ResponseEntity.ok(instances.stream().map(t -> {
			try {
				return produce(t);
			} catch (UnauthorizedCredential e) {
				return e.getMessage();
			}
		}).collect(Collectors.toList()));
	}

	protected <T extends DomainEntity> ResponseEntity<?> send(T instance, String messageIfNull)
			throws UnauthorizedCredential {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(produce(instance));
	}

	protected <T extends DomainEntity, E extends T> ResponseEntity<?> send(E instance, Class<E> type,
			String messageIfNull) throws UnauthorizedCredential {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(produce(instance, type));
	}

	protected <T extends DomainEntity, E extends T> ResponseEntity<?> send(E instance, Class<E> type,
			String messageIfNull, Credential credential) throws UnauthorizedCredential {
		return instance == null ? sendNotFound(messageIfNull) : ResponseEntity.ok(produce(instance, type, credential));
	}

	protected <T> ResponseEntity<?> fails(T instance) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(instance);
	}

	protected <T extends Entity> ResponseEntity<?> send(Result<T> result) throws Exception {
		if (result.isOk()) {
			return ResponseEntity.ok(produce(result.getInstance()));
		}

		if (result.getStatus() == Status.BAD) {
			return sendBad(result.getMessages());
		}

		return fails(result.getMessages());
	}

	protected <T> ResponseEntity<?> makeStaleWhileRevalidate(T body, long maxAge, TimeUnit maxAgeDurationUnit,
			long revalidateDuration, TimeUnit revalidateDurationUnit) {
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(maxAge, maxAgeDurationUnit)
				.staleWhileRevalidate(revalidateDuration, maxAgeDurationUnit)).body(body);
	}

}
