/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.LockMode;
import org.hibernate.engine.spi.Status;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceContext {

	Object find(ResourceKey<?> key);

	// @formatter:off
	ResourceEntry<?> addResource(
			Object instance,
			Status status,
			LockMode lockMode,
			ResourceKey<?> key,
			ResourceDescriptor<?> descriptor
	);
	// @formatter:on
	void remove(Serializable identifier);

	void clear();

	boolean contains(Serializable pathName);

	ResourceManager getResourceManager();

	<T> ResourceEntry<T> getEntry(Object entity);

	// @formatter:off
	<T> ResourceEntry<T> addEntry(
			String resourceName,
			T instance,
			Status status,
			Status prevStatus,
			Object[] loadedState,
			Object[] deletedState,
			LockMode lockMode,
			ResourceKey<T> key,
			boolean isTransient,
			ResourceDescriptor<T> descriptor);
	// @formatter:on

}
