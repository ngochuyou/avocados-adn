/**
 * 
 */
package adn.model.factory.authentication;

import java.util.UUID;
import java.util.function.BiFunction;

import adn.model.DomainEntity;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelProducerFactoryBuilder {

	<T extends DomainEntity> WithType<T> type(Class<T> type);

	interface WithType<T extends DomainEntity> {

		WithCredential<T> roles(Role... role);

		WithCredential<T> departments(UUID... departmentId);

		WithCredential<T> with(Role[] role, UUID[] departmentId);

		WithCredential<T> credentials(Credential... id);

	}

	interface WithCredential<T extends DomainEntity> {

		WithField<T> fields(String... fields);

		WithCredential<T> roles(Role... role);

		WithCredential<T> departments(UUID... departmentId);

		WithCredential<T> with(Role[] role, UUID[] departmentId);

		WithCredential<T> credentials(Credential... credential);

		WithCredential<T> mask();

		WithCredential<T> publish();

		WithType<T> type();

	}

	interface WithField<T extends DomainEntity> {

		WithField<T> use(String... alias);

		WithField<T> use(BiFunction<Arguments<?>, Credential, ?>[] fncs);

		WithField<T> publish();

		WithField<T> mask();

		WithField<T> anyFields();

		WithField<T> fields(String... fields);

		WithCredential<T> roles(Role... roles);

		WithCredential<T> departments(UUID... departments);

		WithCredential<T> with(Role[] roles, UUID[] departments);

		WithCredential<T> credentials(Credential... credentials);

		WithType<T> type(Class<T> type);

	}

}