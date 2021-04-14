/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourcePersister;
import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public class PersistentEvent<T> extends EventImpl<T> {

	protected static final long serialVersionUID = 1L;

	protected final T instance;

	protected final ResourcePersister<T> resourceDescriptor;

	PersistentEvent(T instance, Class<T> type, ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		super(resourceManager);
		this.instance = instance;
		this.resourceDescriptor = resourceManager.getResourceManagerFactory().locateResourceDescriptor(type);
	}

	public T getInstance() {
		return instance;
	}

	public ResourcePersister<T> getResourceDescriptor() {
		return resourceDescriptor;
	}

}
