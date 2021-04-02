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
	public boolean contains(Serializable pathName) {
		// TODO Auto-generated method stub
		return context.containsKey(pathName);
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

	@Override
	public <T> ResourceEntry<T> getEntry(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}
	// @formatter:off

	@Override
	public <T> ResourceEntry<T> addEntry(
			String resourceName,
			T instance,
			Status status,
			Status prevStatus,
			Object[] loadedState,
			Object[] deletedState,
			LockMode lockMode,
			ResourceKey<T> key,
			boolean isTransient,
			ResourceDescriptor<T> descriptor) {
		// TODO Auto-generated method stub
		if (!entryContext.containsKey(instance)) {
			return new ResourceEntryImpl<>(
					resourceName,
					key.getIdentifier(),
					Status.MANAGED,
					null,
					null,
					null,
					instance,
					lockMode,
					isTransient,
					key.getDescriptor(),
					key);
		}
		
		return null;
	}
	// @formatter:on

	@Override
	public ResourceEntry<?> addResource(Object instance, Status status, LockMode lockMode, ResourceKey<?> key,
			ResourceDescriptor<?> descriptor) {
		// TODO Auto-generated method stub
		context.computeIfAbsent(key, k -> {
			return instance;
		});

		return null;
	}

}
