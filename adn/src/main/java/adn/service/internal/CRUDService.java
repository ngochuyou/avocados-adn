/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import adn.application.context.ContextProvider;
import adn.dao.generic.Result;
import adn.dao.generic.ResultBatch;
import adn.model.entities.Entity;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
public interface CRUDService extends Service {

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

	default Session getCurrentSession() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();
	}

	<T extends Entity> List<String> getDefaultColumns(Class<T> type, Credential credential, Collection<String> columns)
			throws NoSuchFieldException;

	<T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Credential credential)
			throws NoSuchFieldException, Exception;

	<T extends Entity> List<Map<String, Object>> readByAssociation(Class<T> type,
			Class<? extends Entity> associatingType, String associatingAttribute, String associationProperty,
			Serializable associationIdentifier, Collection<String> columns, Pageable pageable, Credential credential,
			SourceMetadata<T> sourceMetadata) throws NoSuchFieldException, Exception;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Sort sort, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Sort sort, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> find(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> find(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Pageable pageable, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> requestedColumns,
			Specification<T> spec, Pageable pageable, Credential credential, SourceMetadata<T> metadata)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> List<Map<String, Object>> read(Class<T> type, Collection<String> columns, Pageable pageable,
			Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential;

	<T extends Entity> Map<String, Object> find(Serializable id, Class<T> type, Collection<String> columns,
			Credential credential, SourceMetadata<T> sourceMetadata)
			throws NoSuchFieldException, UnauthorizedCredential;
}
