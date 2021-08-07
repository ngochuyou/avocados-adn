/**
 * 
 */
package adn.model.factory.property.production.department;

import java.util.UUID;
import java.util.function.Function;

import adn.model.DepartmentScoped;

/**
 * @author Ngoc Huy
 *
 */
public interface DepartmentBasedModelPropertiesProducersBuilder {

	<T extends DepartmentScoped> WithType<T> type(Class<T> type);

	interface Owned {

		DepartmentBasedModelPropertiesProducersBuilder and();

	}

	interface WithType<T extends DepartmentScoped> extends Owned {

		WithDepartment department(String loggableName, UUID... departmentId);

	}

	interface WithDepartment extends Owned {

		WithField fields(String... fieldNames);

		WithDepartment mask();

		WithDepartment publish();
		
		WithDepartment department(UUID departmentId, String loggableName);

	}

	interface WithField extends Owned {

		<F, R> WithField use(Function<F, R> fnc);

		WithField mask();

		WithField publish();

		WithField fields(String... fieldNames);
		
		WithField others();

		WithDepartment department(UUID departmentId, String loggableName);

	}

}
