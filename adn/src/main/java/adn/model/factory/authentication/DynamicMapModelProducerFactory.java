/**
 * 
 */
package adn.model.factory.authentication;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.model.factory.authentication.dynamicmap.DynamicMapModelProducer;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
public interface DynamicMapModelProducerFactory {

	<T extends DomainEntity, E extends T> Map<String, Object> produce(Object[] source, SourceMetadata<E> metadata,
			Credential credential) throws UnauthorizedCredential;

	<T extends DomainEntity, E extends T> List<Map<String, Object>> produce(List<Object[]> source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential;

	<T extends DomainEntity, E extends T> Map<String, Object> produceSingular(Object source, SourceMetadata<E> metadata,
			Credential credential) throws UnauthorizedCredential;

	<T extends DomainEntity, E extends T> List<Map<String, Object>> produceSingular(List<Object> source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential;

	<T extends DomainEntity, E extends T> Map<String, Object> producePojo(E entity, SourceMetadata<E> metadata,
			Credential credential) throws UnauthorizedCredential;

	<T extends DomainEntity, E extends T> List<Map<String, Object>> producePojo(List<E> source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential;

	<T extends DomainEntity> DynamicMapModelProducer<T> getProducer(Class<T> entityType);

	<T extends DomainEntity> List<String> validateColumns(Class<T> entityType,
			Collection<String> requestedColumnNames, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential;

}
