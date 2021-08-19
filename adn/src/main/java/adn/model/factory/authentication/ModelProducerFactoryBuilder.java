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

		WithCredential<T> role(Role role);

		WithCredential<T> department(UUID departmentId);
		
		WithCredential<T> with(Role role, UUID departmentId);

		WithCredential<T> credential(Credential id);

	}

	interface WithCredential<T extends DomainEntity> {

		WithCredential<T> role(Role role);

		WithCredential<T> department(UUID departmentId);
		
		WithCredential<T> with(Role role, UUID departmentId);

		WithCredential<T> credential(Credential credential);

		WithField<T> fields(String... fields);

		WithCredential<T> mask();

		WithCredential<T> publish();

	}

	interface WithField<T extends DomainEntity> {

		WithField<T> use(String... alias);

		WithField<T> use(BiFunction<Object, Credential, Object>[] fncs);

		WithField<T> publish();

		WithField<T> mask();

		WithField<T> anyFields();

	}

}
