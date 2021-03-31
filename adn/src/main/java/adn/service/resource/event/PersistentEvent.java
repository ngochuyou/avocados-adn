/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceDescriptor;
import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public class PersistentEvent<T> extends EventImpl {

	protected static final long serialVersionUID = 1L;

	protected final T instance;

	protected final ResourceDescriptor<T> resourceDescriptor;

	PersistentEvent(T instance, Class<T> type, ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		super(resourceManager);
		this.instance = instance;
		this.resourceDescriptor = resourceManager.getResourceManagerFactory().locateResourceDescriptor(type);
	}

	public T getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <E> E getInstanceAndUnwrap() {
		return (E) instance;
	}

	public ResourceDescriptor<T> getResourceDescriptor() {
		return resourceDescriptor;
	}

}
