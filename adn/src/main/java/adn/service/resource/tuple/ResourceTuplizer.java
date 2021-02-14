/**
 * 
 */
package adn.service.resource.tuple;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import adn.service.resource.metamodel.EntityMode;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTuplizer {

	Class<?> getMappedClass();
	
	EntityMode getEntityMode();

	Object instantiate(Serializable id, EntityManager resoureManager) throws PersistenceException;

	Serializable getIdentifier(Object resource, EntityManager resoureManager) throws PersistenceException;

	void setIdentifier(Object resource, Serializable id, EntityManager resourceManager) throws PersistenceException;

	Object getVersion(Object resource) throws PersistenceException;

	void setPropertyValue(Object resource, int i, Object value) throws PersistenceException;

	void setPropertyValue(Object resource, String propertyName, Object value) throws PersistenceException;

	Object getPropertyValue(Object resource, String propertyName) throws PersistenceException;

	public void setPropertyValues(Object resource, Object[] values) throws PersistenceException;
	
	Object[] getPropertyValues(Object resource) throws PersistenceException;

	boolean isInstance(Object resource) throws PersistenceException;

	void afterInitialize(Object resource, EntityManager resourceManager);

}
