/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.File;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate {

	String getName();

	String getPathColumnName();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	ResourceInstantiator<File> getInstantiator();

	PropertyAccessImplementor[] getPropertyAccessors();

}
