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
	ResourceEntry<?> addEntry(
			Object instance,
			Status status,
			Object[] loadedState,
			Serializable id,
			LockMode lockMode,
			boolean isTransient,
			ResourceDescriptor<?> descriptor);
	// @formatter:on
	// @formatter:off
	ResourceEntry<?> addResource(
			Object instance,
			Status status,
			Object[] loadedState,
			ResourceKey<?> key,
			LockMode lockMode,
			boolean isTransient,
			ResourceDescriptor<?> descriptor);
	// @formatter:on

	void setEntryStatus(ResourceEntry<?> entry, Status status);

	void addResource(ResourceKey<?> key, Object instance);

	Object getResource(ResourceKey<?> key);

	Object removeResource(ResourceKey<?> key);

	ResourceEntry<?> getEntry(Object instance);

	ResourceEntry<?> removeEntry(Object instance);

	boolean hasEntry(Object instance);
	
	void remove(Serializable identifier);

	void clear();

	boolean contains(ResourceKey<?> key);

	ResourceManager getResourceManager();

}
