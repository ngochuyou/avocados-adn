/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.application.Result;
import adn.dao.generic.ResultBatch;
import adn.model.entities.Entity;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericCRUDService extends Service {

	default <T extends Entity, E extends T> Result<E> create(Serializable id, E model, Class<E> type) {
		return create(id, model, type, false);
	}

	<T extends Entity, E extends T> Result<E> create(Serializable id, E model, Class<E> type, boolean flushOnFinish);

	default <T extends Entity, E extends T> ResultBatch<E> createBatch(Collection<E> batch, Class<E> type) {
		return createBatch(batch, type, false);
	};

	<T extends Entity, E extends T> ResultBatch<E> createBatch(Collection<E> batch, Class<E> type,
			boolean flushOnFinish);

	default <T extends Entity, E extends T> Result<E> update(Serializable id, E model, Class<E> type) {
		return update(id, model, type, false);
	}

	<T extends Entity, E extends T> Result<E> update(Serializable id, E model, Class<E> type, boolean flushOnFinish);

	// @formatter:off
	<T extends Entity> List<Map<String, Object>> readAllByAssociation(
			Class<T> type,
			Class<? extends Entity> associatingType,
			String associatingAttribute,
			String associationProperty,
			Serializable associationIdentifier,
			Collection<String> columns,
			Pageable pageable,
			Credential credential)
			throws NoSuchFieldException, Exception;

	<T extends Entity> List<Map<String, Object>> readAllByAssociation(
			Class<T> type,
			Class<? extends Entity> associatingType,
			String associatingAttribute,
			String associationProperty,
			Serializable associationIdentifier,
			Pageable pageable,
			Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, Exception;
	
	<T extends Entity> List<Map<String, Object>> readAllByAssociation(
			Class<T> type,
			Class<? extends Entity> associatingType,
			String associatingAttribute,
			String associationProperty,
			Serializable associationValue,
			Collection<String> columns,
			Pageable pageable,
			Credential credential,
			Specification<T> spec)
			throws NoSuchFieldException, Exception;

	<T extends Entity> List<Map<String, Object>> readAllByAssociation(
			Class<T> type,
			Class<? extends Entity> associatingType,
			String associatingAttribute,
			String associationProperty,
			Serializable associationValue,
			Pageable pageable,
			Credential credential,
			SourceMetadata<T> sourceMetadata,
			Specification<T> spec) throws NoSuchFieldException, Exception;
	// @formatter:on
	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Sort sort, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec, Sort sort,
			Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Credential credential, Function<SourceMetadata<T>, List<Object[]>> sourceSupplier)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> read(Class<T> type, Specification<T> spec, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Pageable pageable, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec, Pageable pageable,
			Credential credential, SourceMetadata<T> metadata) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> columns, Pageable pageable,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> readAll(Class<T> type, Pageable pageable, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> readById(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> readById(Serializable id, Class<T> type, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, UnauthorizedCredential;

}
