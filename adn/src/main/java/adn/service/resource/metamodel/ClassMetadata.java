/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public interface ClassMetadata {

	String getResourceName();

	String getIdentifierPropertyName();

	String[] getPropertyNames();

	Type<?> getIdentifierType();

	Type<?>[] getPropertyTypes();

	Type<?> getPropertyType(String propertyName) throws PersistenceException;

	boolean isMutable();

	boolean isVersioned();

	int getVersionProperty();

	boolean[] getPropertyNullability();

	boolean hasIdentifierProperty();

	boolean hasSubclasses();

	boolean isInherited();

	Class<?> getMappedClass();

	Object instantiate(Serializable id, EntityManager resourceManager);

	Object getPropertyValue(Object object, String propertyName) throws PersistenceException;

	Object[] getPropertyValues(Object resource) throws PersistenceException;

	void setPropertyValue(Object object, String propertyName, Object value) throws PersistenceException;

	void setPropertyValues(Object object, Object[] values) throws PersistenceException;

	Serializable getIdentifier(Object resource, EntityManager resourceManager);

	void setIdentifier(Object resource, Serializable id, EntityManager resourceManager);

	Object getVersion(Object object) throws PersistenceException;

}
