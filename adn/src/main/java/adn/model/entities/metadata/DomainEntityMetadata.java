/**
 * 
 */
package adn.model.entities.metadata;

import java.util.Map;
import java.util.Set;

import org.hibernate.property.access.spi.Getter;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface DomainEntityMetadata {

	boolean hasProperty(String attributeName);

	boolean isEntityType(String attributeName);

	boolean isAssociation(String attributeName);

	Class<?> getPropertyType(String attributeName);

	Class<? extends DomainEntity> getAssociationType(String associationName);

	Set<String> getPropertyNames();

	Set<String> getNonLazyPropertyNames();

	Set<String> getDeclaredPropertyNames();

	int getNonLazyPropertiesSpan();

	int getPropertiesSpan();

	String getDiscriminatorColumnName();

	Set<Map.Entry<String, Getter>> getGetters();

}
