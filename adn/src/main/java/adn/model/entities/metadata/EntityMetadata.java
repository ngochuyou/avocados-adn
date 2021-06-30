/**
 * 
 */
package adn.model.entities.metadata;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityMetadata {

	boolean hasAttribute(String attributeName);

	<T> String validate(String attributeName, T value);

	<T> String buildAttribute(String attributeName, T value);

	<T> T produce(String attributeName, T value, Role role);

}
