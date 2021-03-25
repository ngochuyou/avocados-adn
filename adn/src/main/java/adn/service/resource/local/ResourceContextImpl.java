/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceContextImpl implements ResourceContext {

	private static final int INIT_MAP_SIZE = 8;
	
	private Map<Serializable, Object> context = new HashMap<>(INIT_MAP_SIZE);

	private final ResourceManager resourceManager;

	/**
	 * 
	 */
	public ResourceContextImpl(ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
	}

	@Override
	public Object find(String pathName) {
		// TODO Auto-generated method stub
		if (context == null || context.isEmpty()) {
			return null;
		}

		return context.getOrDefault(pathName, null);
	}

	@Override
	public void add(Serializable pathname, Object resource) {
		// TODO Auto-generated method stub
		if (context == null) {
			context = new HashMap<>(8);
		}

		context.put(pathname, resource);
	}

	@Override
	public void remove(String pathName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(String pathName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

}
