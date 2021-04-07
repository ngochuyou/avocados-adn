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

	public <T> PersistentEvent<T> createManageEvent(T instance, Class<T> type, ResourceManager resourceManager) {
		return new PersistentEvent<T>(instance, type, resourceManager);
	}

//
//	public <T> SaveEvent<T> createSaveEvent(T instance, Class<T> type, ResourceManager resourceManager) {
//		ResourceDescriptor<T> descriptor = resourceManager.getResourceManagerFactory().locateResourceDescriptor(type);
//
//		if (descriptor.isIdentifierAutoGenerated()
//				&& descriptor.getIdentifierValueGeneration().getGenerationTiming().equals(GenerationTiming.INSERT)) {
//
//			return new SaveEvent.IdentifierGenerationRequiredSaveEvent<T>(resourceManager, instance, type);
//		}
//
//		return new SaveEvent.SaveEventImpl<T>(resourceManager, instance, type);
//	}

}