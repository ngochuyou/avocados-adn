/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractEvent implements Event {

	private static final long serialVersionUID = 1L;

	protected final transient ResourceManager resourceManager;

	public AbstractEvent(final ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
	}

	@Override
	public ResourceManager getResourceManager() {
		return resourceManager;
	}

}
