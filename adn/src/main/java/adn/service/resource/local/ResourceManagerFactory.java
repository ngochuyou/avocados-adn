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

	public static final String HIBERNATE_UNSUPPORTED = "Some of Hibernate implementations might be unsupported";

	ResourceManager getResourceManager();

	<T> ResourceDescriptor<T> getResourceDescriptor(String name);

	<T> ResourceDescriptor<T> getResourceDescriptor(Class<T> clazz);

	boolean isLocked(Serializable identifier);

	boolean setLocked(Serializable identifier, boolean isLocked);

}