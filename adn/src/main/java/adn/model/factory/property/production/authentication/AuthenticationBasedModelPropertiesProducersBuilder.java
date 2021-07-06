/**
 * 
 */
package adn.model.factory.property.production.authentication;

import java.util.function.Function;

import adn.model.AbstractModel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelPropertiesProducersBuilder {

	<T extends AbstractModel> WithType<T> type(Class<T> type);

	<T extends AbstractModel, E extends T> WithType<E> types(@SuppressWarnings("unchecked") Class<E>... types);

	AuthenticationBasedModelPropertiesProducersBuilder mask();

	AuthenticationBasedModelPropertiesProducersBuilder publish();

	AuthenticationBasedModelPropertiesProducersBuilder maskUngivenTypes();

	AuthenticationBasedModelPropertiesProducersBuilder publishUngivenTypes();

	public interface Owned {

		AuthenticationBasedModelPropertiesProducersBuilder and();

	}

	public interface WithType<T extends AbstractModel> extends Owned {

		WithRole<T> role(Role role);

		WithRole<T> roles(Role... roles);

		WithType<T> mask();

		WithType<T> publish();

		WithType<T> maskUngivenRoles();

		WithType<T> publishUngivenRoles();

	}

	public interface WithRole<T extends AbstractModel> extends Owned {

		WithField<T> field(String fieldName);

		WithField<T> fields(String... fieldNames);

		WithRole<T> mask();

		WithRole<T> publish();

		WithType<T> more();

		WithRole<T> maskUngivenFields();

		WithRole<T> publishUngivenFields();

	}

	public interface WithField<T extends AbstractModel> extends Owned {

		WithField<T> use(String alternativeName);

		WithField<T> mask();

		WithField<T> publish();

		<F, R> WithField<T> use(Function<F, R> function);

		WithRole<T> more();

	}

}
