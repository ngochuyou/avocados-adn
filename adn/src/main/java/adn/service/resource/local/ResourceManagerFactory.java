/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;

import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManagerFactory extends EntityManagerFactory {

	<T> ResourcePersister<T> getResourceDescriptor(String name);

	<T> ResourcePersister<T> getResourceDescriptor(Class<T> clazz);

	<T> ResourcePersister<T> locateResourceDescriptor(Class<T> clazz) throws IllegalArgumentException;

	boolean isLocked(Serializable identifier);

	boolean setLocked(Serializable identifier, boolean isLocked);

	LocalResourceStorage getStorage();

	ContextBuildingService getContextBuildingService();

	SharedIdentifierGeneratorFactory getIdentifierGeneratorFactory();
	
	public interface ServiceWrapper<T> extends Service {

		T unwrap();

	}

}
