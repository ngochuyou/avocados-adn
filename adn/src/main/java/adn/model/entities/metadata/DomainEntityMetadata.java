/**
 * 
 */
package adn.model.entities.metadata;

import java.util.List;
import java.util.Map;

import org.hibernate.property.access.spi.Getter;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface DomainEntityMetadata<T> {

	Class<T> getType();

	boolean hasProperty(String attributeName);

	boolean isEntityType(String attributeName);

	boolean isAssociation(String attributeName);

	boolean isAssociationCollection(String attributeName);

	Class<?> getPropertyType(String attributeName);

	Class<? extends DomainEntity> getAssociationType(String associationName);

	List<String> getPropertyNames();

	List<String> getNonLazyPropertyNames();

	List<String> getDeclaredPropertyNames();

	int getPropertiesSpan();

	String getDiscriminatorColumnName();

	List<Map.Entry<String, Getter>> getGetters();

}
