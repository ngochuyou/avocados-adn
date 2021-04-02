/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManagerFactory {

	ResourceManager getResourceManager();

	<T> ResourceDescriptor<T> getResourceDescriptor(String name);

	<T> ResourceDescriptor<T> getResourceDescriptor(Class<T> clazz);

	<T> ResourceDescriptor<T> locateResourceDescriptor(Class<T> clazz) throws IllegalArgumentException;

	boolean isLocked(Serializable identifier);

	boolean setLocked(Serializable identifier, boolean isLocked);
	
	LocalResourceStorage getStorage();

	public static void unsupportHBN() {
		throw new UnsupportedOperationException("Some of Hibernate implementations might be unsupported");
	}

}
