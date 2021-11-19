/**
 * 
 */
package adn.model.factory.authentication;

import java.util.UUID;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.model.DomainEntity;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelProducerFactoryBuilder {

	<T extends DomainEntity> WithType<T> type(Class<T> type);

	public interface WithType<T extends DomainEntity> {

		WithCredential<T> roles(Role... role);

		WithCredential<T> departments(UUID... departmentId);

		WithCredential<T> with(Role[] role, UUID[] departmentId);

		WithCredential<T> credentials(Credential... id);

	}

	public interface WithCredential<T extends DomainEntity> {

		WithField<T> fields(String... fields);

		WithCredential<T> roles(Role... role);

		WithCredential<T> departments(UUID... departmentId);

		WithCredential<T> with(Role[] role, UUID[] departmentId);

		WithCredential<T> credentials(Credential... credential);

		WithCredential<T> mask();

		WithCredential<T> publish();

		<E extends DomainEntity> WithType<E> type(Class<E> type);

	}

	public interface WithField<T extends DomainEntity> {

		WithField<T> use(String... alias);

		@SuppressWarnings("unchecked")
		WithField<T> useFunction(HandledBiFunction<Arguments<?>, Credential, ?, Exception>... fncs);

		WithField<T> publish();

		WithField<T> mask();

		WithField<T> anyFields();

		WithField<T> fields(String... fields);

		WithCredential<T> roles(Role... roles);

		WithCredential<T> departments(UUID... departments);

		WithCredential<T> with(Role[] roles, UUID[] departments);

		WithCredential<T> credentials(Credential... credentials);

		<E extends DomainEntity> WithType<E> type(Class<E> type);

	}

}
