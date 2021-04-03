/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.engine.spi.Status;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceContextImpl implements ResourceContext {

	private static final int INIT_MAP_SIZE = 8;

	private final Map<ResourceKey<?>, Object> context = new HashMap<>(INIT_MAP_SIZE);

	private final Map<Object, ResourceEntry<?>> entryContext = new IdentityHashMap<>(INIT_MAP_SIZE);

	private final ResourceManager resourceManager;

	/**
	 * 
	 */
	public ResourceContextImpl(ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
	}

	@Override
	public Object find(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		if (context == null || context.isEmpty()) {
			return null;
		}

		return context.get(key);
	}

	@Override
	public void remove(Serializable pathName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

	// @formatter:off
	@Override
	public ResourceEntry<?> addEntry(
			Object instance,
			Status status,
			Object[] loadedState,
			Serializable id,
			LockMode lockMode,
			boolean isTransient,
			ResourceDescriptor<?> descriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceEntry<?> addResource(
			Object instance,
			Status status,
			Object[] loadedState,
			ResourceKey<?> key,
			LockMode lockMode,
			boolean isTransient,
			ResourceDescriptor<?> descriptor) {
		// TODO Auto-generated method stub
		return null;
	}
	// @formatter:on

	@Override
	public void setEntryStatus(ResourceEntry<?> entry, Status status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addResource(ResourceKey<?> key, Object instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getResource(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object removeResource(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceEntry<?> getEntry(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceEntry<?> removeEntry(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasEntry(Object instance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		return false;
	}

}
