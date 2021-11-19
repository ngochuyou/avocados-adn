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

	int getPropertiesSpan();

	boolean hasProperty(String attributeName);
	
	Class<?> getPropertyType(String attributeName);

	List<String> getPropertyNames();

	List<String> getNonLazyPropertyNames();

	Map<String, Getter> getGetters();

	boolean isAssociation(String attributeName);

	boolean isAssociationOptional(String attributeName);
	
	AssociationType getAssociationType(String associationName);

	Class<? extends DomainEntity> getAssociationClass(String associationName);

}
