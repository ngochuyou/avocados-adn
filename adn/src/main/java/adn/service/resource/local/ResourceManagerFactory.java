/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManagerFactory extends EntityManagerFactory {

	<T> ResourceDescriptor<T> getResourceDescriptor(String name);

	<T> ResourceDescriptor<T> getResourceDescriptor(Class<T> clazz);

	<T> ResourceDescriptor<T> locateResourceDescriptor(Class<T> clazz) throws IllegalArgumentException;

	boolean isLocked(Serializable identifier);

	boolean setLocked(Serializable identifier, boolean isLocked);

	LocalResourceStorage getStorage();

	ContextBuildingService getContextBuildingService();

	public interface ServiceWrapper<T> extends Service {

		T unwrap();

	}

}
