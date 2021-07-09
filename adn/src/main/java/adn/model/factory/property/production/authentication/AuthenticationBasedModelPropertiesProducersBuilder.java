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

	/**
	 * With these types
	 */
	<T extends AbstractModel, E extends T> WithType<E> type(Class<E> type);

	/**
	 * With these types
	 */
	<T extends AbstractModel, E extends T> WithType<E> type(Class<E>[] types);

	/**
	 * Mask every {@link AbstractModel}
	 */
	AuthenticationBasedModelPropertiesProducersBuilder mask();

	/**
	 * Publish every {@link AbstractModel}
	 */
	AuthenticationBasedModelPropertiesProducersBuilder publish();

	/**
	 * With every other types
	 */
	<T extends AbstractModel> WithType<T> ungivenTypes();

	public interface Owned {

		/**
		 * Get back to {@link AuthenticationBasedModelPropertiesProducersBuilder} level
		 */
		AuthenticationBasedModelPropertiesProducersBuilder and();

		
		
	}
	
	

	public interface WithType<T extends AbstractModel> extends Owned {

		/**
		 * With these {@link Role}
		 */
		WithRole<T> role(Role... role);

		/**
		 * Mask everything against very roles in these types
		 */
		WithType<T> mask();

		/**
		 * Publish everything for very roles in these types
		 */
		WithType<T> publish();

		/**
		 * With every other roles in these types
		 */
		WithRole<T> anyRoles();

	}

	public interface WithRole<T extends AbstractModel> extends Owned {

		/**
		 * With these fields
		 */
		WithField<T> field(String... field);

		/**
		 * Mask everything against every given roles of this instance,
		 */
		WithRole<T> mask();

		/**
		 * Publish everything for every given roles of this instance,
		 */
		WithRole<T> publish();

		/**
		 * With every other roles of these types
		 */
		WithRole<T> anyRoles();

		/**
		 * With every other fields for the given roles in this instance
		 */
		WithField<T> anyFields();

		/**
		 * Get back to these types level
		 */
		WithType<T> type();

	}

	public interface WithField<T extends AbstractModel> extends Owned {

		/**
		 * Get back to role level, with this field
		 */
		WithField<T> field(String field);

		/**
		 * Get back to role level, with these fields
		 */
		WithField<T> fields(String... field);

		/**
		 * use this alternative name
		 */
		WithField<T> use(String alternativeName);

		/**
		 * use this function
		 */
		<F, R> WithField<T> use(Function<F, R> function);

		/**
		 * Mask these fields
		 */
		WithField<T> mask();

		/**
		 * Publish these fields
		 */
		WithField<T> publish();

		/**
		 * With the given fields of this instance, against this other role, in these
		 * types
		 */
		WithField<T> role(Role role);

		/**
		 * With the given fields of this instance, against these other roles, in these
		 * types
		 */
		WithField<T> roles(Role... roles);

		/**
		 * With the given fields of this instance, against every other roles, in these
		 * types
		 */
		WithField<T> anyRoles();

		/**
		 * With every other fields, against these roles, in these types
		 */
		WithField<T> anyFields();

		/**
		 * Exclude these fields
		 */
		WithField<T> but(String... excludedField);

		/**
		 * Get back to these roles level
		 */
		WithRole<T> role();

		/**
		 * Get back to these types level
		 */
		WithType<T> type();

	}

}
