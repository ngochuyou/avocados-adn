/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class EventImpl<T> implements Event<T> {

	private final ResourceManager resourceManager;

	/**
	 * 
	 */
	public EventImpl(ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

}
