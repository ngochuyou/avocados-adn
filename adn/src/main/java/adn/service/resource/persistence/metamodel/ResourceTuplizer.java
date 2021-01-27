/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.PersistenceException;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTuplizer {

	Object[] getPropertyValues(Object resource) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException;

	void setPropertyValues(Object resource, Object[] values) throws IllegalAccessException, IllegalArgumentException;

	Object getPropertyValue(Object resource, int i) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	Object getPropertyValue(Object resource, String propertyName) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	void setPropertyValue(Object resource, int i, Object value) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	void setPropertyValue(Object resource, String propertyName, Object value) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	Object instantiate() throws PersistenceException;

	boolean isInstance(Object resource);

	<T> Class<T> getMappedClass();

	Serializable getIdentifier(Object resource)
			throws ClassCastException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	void setIdentifier(Object resource, Serializable value)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	int getPropertySpan();

}
