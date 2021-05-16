/**
 * 
 */
package adn.service.resource.engine.template;

import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.tuple.Instantiator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate {

	String getName();

	Class<?> getSystemType();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	Instantiator getInstantiator();

	PropertyAccess[] getPropertyAccessors();

}
