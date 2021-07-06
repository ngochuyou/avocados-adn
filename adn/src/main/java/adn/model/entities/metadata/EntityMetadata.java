/**
 * 
 */
package adn.model.entities.metadata;

import java.util.Map;
import java.util.Set;

import org.hibernate.property.access.spi.Getter;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityMetadata {

	boolean hasAttribute(String attributeName);

	Set<String> getPropertyNames();

	Set<Map.Entry<String, Getter>> getGetters();

}
