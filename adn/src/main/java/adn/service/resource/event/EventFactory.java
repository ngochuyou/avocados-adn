/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public class EventFactory {

	public static final EventFactory INSTANCE = new EventFactory();

	private EventFactory() {}

	public <T> ManageEvent<T> createManageEvent(T instance, Class<T> type, ResourceManager resourceManager) {
		if (resourceManager.getResourceManagerFactory().getResourceDescriptor(type)
				.getIdentifierValueGeneration() != null) {
			return new ManageEvent.UniqueManageEvent<T>(resourceManager, instance, type);
		}

		return new ManageEvent.ManageEventImpl<T>(resourceManager, instance, type);
	}

}
