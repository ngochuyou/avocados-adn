/**
 * 
 */
package adn.service.resource.event;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.local.ResourceDescriptor;
import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public abstract class SaveEvent<T> extends AbstractPersistentEvent<T> {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SaveEvent(ResourceManager resourceManager, T instance, Class<T> type) {
		// TODO Auto-generated constructor stub
		super(resourceManager, instance, type);
	}

	@Override
	public void preFire() {
		// TODO Auto-generated method stub
		if (resourceManager.find(getResourceDescriptor().getIdentifier(instance), type) == null) {
			resourceManager.manage(instance, type);
		}
		// check lock
	}

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		try {
			logger.trace("Firing save event. Instance type: " + type);

			preFire();
			
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to fire save event. " + e.getMessage());
		}
	}

	public static class IdentifierGenerationRequiredSaveEvent<E> extends SaveEvent<E> {

		private static final long serialVersionUID = 1L;

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		IdentifierGenerationRequiredSaveEvent(ResourceManager resourceManager, E instance, Class<E> type) {
			super(resourceManager, instance, type);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void preFire() {
			// TODO Auto-generated method stub
			super.preFire();

			ResourceDescriptor<E> descriptor = getResourceDescriptor();
			Serializable identifier;

			descriptor.setIdentifier(instance, identifier = descriptor.getIdentifierValueGeneration()
					.getValueGenerator().generateValue(resourceManager.getResourceManagerFactory(), instance));

			logger.trace("Generating identifier for resource of type " + type + ", identifier: " + identifier);
		}

	}

	public static class SaveEventImpl<E> extends SaveEvent<E> {

		private static final long serialVersionUID = 1L;

		public SaveEventImpl(ResourceManager resourceManager, E instance, Class<E> type) {
			super(resourceManager, instance, type);
			// TODO Auto-generated constructor stub
		}

	}

}
