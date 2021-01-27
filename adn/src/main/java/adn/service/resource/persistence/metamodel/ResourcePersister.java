/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister {

	Serializable getIdentifier(Object resource);

	EntityManager getEntityManager();

	String getEntityName();

	ResourceTuplizer getTuplizer();

	boolean isVersioned();

	void setIdentifier(Object resource, Serializable id) throws IllegalAccessException, IllegalArgumentException;

	Object instantiate(Serializable id) throws PersistenceException;

	Object getVersion(Object resource) throws IllegalAccessException;

	void setPropertyValues(Object resource, Object[] values) throws IllegalAccessException, IllegalArgumentException;

	void setPropertyValue(Object resource, int i, Object value) throws IllegalAccessException, IllegalArgumentException;

	Object[] getPropertyValues(Object resource);

	Object getPropertyValue(Object resource, int i) throws IllegalAccessException;

	Object getPropertyValue(Object resource, String propertyName);

}
