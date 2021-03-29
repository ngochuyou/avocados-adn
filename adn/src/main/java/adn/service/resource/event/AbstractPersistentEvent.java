/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceContext;
import adn.service.resource.local.ResourceDescriptor;
import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractPersistentEvent<T> extends AbstractEvent {

	protected static final long serialVersionUID = 1L;

	protected final T instance;

	protected final Class<T> type;

	public AbstractPersistentEvent(ResourceManager resourceManager, T instance, Class<T> type) {
		// TODO Auto-generated constructor stub
		super(resourceManager);
		this.instance = instance;
		this.type = type;
	}

	protected ResourceDescriptor<T> getResourceDescriptor() {

		return resourceManager.getResourceManagerFactory().locateResourceDescriptor(type);
	}

	public T getInstance() {
		return instance;
	}

	public Class<T> getType() {
		return type;
	}

	protected ResourceContext getResourceContext() {

		return resourceManager.getResourceContext();
	}

}
