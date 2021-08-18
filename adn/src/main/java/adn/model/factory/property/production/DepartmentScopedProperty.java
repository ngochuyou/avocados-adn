/**
 * 
 */
package adn.model.factory.property.production;

import java.util.UUID;
import java.util.function.Function;

import adn.model.DepartmentScoped;

/**
 * @author Ngoc Huy
 *
 */
public interface DepartmentScopedProperty<T extends DepartmentScoped> {

	Class<T> getEntityType();

	String getName();

	UUID getDepartmentId();

	Function<Object, Object> getFunction();

}
