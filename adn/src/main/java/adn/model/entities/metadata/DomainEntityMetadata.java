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
public interface DomainEntityMetadata {

	boolean hasProperty(String propertyName);
	
	boolean isEntityType(String propertyName);

	Class<?> getPropertyType(String propertyName);
	
	Set<String> getPropertyNames();

	Set<String> getNonLazyPropertyNames();

	Set<String> getDeclaredPropertyNames();

	int getNonLazyPropertiesSpan();

	int getPropertiesSpan();

	String getDiscriminatorColumnName();

	Set<Map.Entry<String, Getter>> getGetters();

}
