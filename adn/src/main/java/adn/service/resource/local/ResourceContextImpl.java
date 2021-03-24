/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceContextImpl implements ResourcePersistenceContext {

	private Map<Serializable, Object> context;

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
		return null;
	}

	@Override
	public void add(Object resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(String pathName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() {
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
	public Serializable getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

}
