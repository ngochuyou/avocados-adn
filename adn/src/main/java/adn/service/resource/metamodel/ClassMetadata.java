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

	String getEntityName();

	String getIdentifierPropertyName();

	String[] getPropertyNames();

	Type<?> getIdentifierType();

	Type<?>[] getPropertyTypes();

	Type<?> getPropertyType(String propertyName) throws PersistenceException;

	boolean hasProxy();

	boolean isMutable();

	boolean isVersioned();

	int getVersionProperty();

	boolean[] getPropertyNullability();

	boolean[] getPropertyLaziness();

	boolean hasIdentifierProperty();

	boolean hasNaturalIdentifier();

	int[] getNaturalIdentifierProperties();

	boolean hasSubclasses();

	boolean isInherited();

	Class<?> getMappedClass();

	Object instantiate(Serializable id, EntityManager session);

	Object getPropertyValue(Object object, String propertyName) throws PersistenceException;

	Object[] getPropertyValues(Object entity) throws PersistenceException;

	void setPropertyValue(Object object, String propertyName, Object value) throws PersistenceException;

	void setPropertyValues(Object object, Object[] values) throws PersistenceException;

	Serializable getIdentifier(Object entity, EntityManager session);

	void setIdentifier(Object entity, Serializable id, EntityManager session);

	Object getVersion(Object object) throws PersistenceException;

}
