/**
 * 
 */
package adn.service.resource.engine.template;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessDelegate;
import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate<T> {

	String getName();

	Class<T> getSystemType();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	ResourceInstantiator<T> getInstantiator();

	PropertyAccessDelegate[] getPropertyAccessors();

}
