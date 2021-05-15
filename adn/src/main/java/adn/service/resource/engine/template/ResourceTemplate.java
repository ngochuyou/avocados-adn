/**
 * 
 */
package adn.service.resource.engine.template;

import org.hibernate.property.access.spi.PropertyAccess;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate {

	String getName();

	Class<?> getSystemType();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	PropertyAccess[] getPropertyAccessors();

}
