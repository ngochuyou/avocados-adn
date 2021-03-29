/**
 * 
 */
package adn.service.resource.event;

import java.io.Serializable;

import org.hibernate.tuple.GenerationTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import adn.service.resource.local.ResourceDescriptor;
import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public abstract class ManageEvent<T> extends AbstractPersistentEvent<T> {

	private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

	public ManageEvent(ResourceManager resourceManager, T instance, Class<T> type) {
		// TODO Auto-generated constructor stub
		super(resourceManager, instance, type);
	}

	abstract public void preManage();

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		try {
			preManage();

			Serializable identifier;

			getResourceContext().add(identifier = getResourceDescriptor().getIdentifier(instance), instance);
			logger.trace("Managing resource of type: " + instance.getClass() + "identifier: " + identifier);
		} catch (Exception e) {
			logger.error("Failed to fire event " + this.getClass() + ". Message: " + e.getMessage());
		}
	}

	public static class ManageEventImpl<E> extends ManageEvent<E> {

		ManageEventImpl(@NonNull ResourceManager resourceManager, @NonNull E instance, @NonNull Class<E> type) {
			super(resourceManager, instance, type);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void preManage() {
			// TODO Auto-generated method stub
		}

	}

	public static class UniqueManageEvent<E> extends ManageEvent<E> {

		UniqueManageEvent(@NonNull ResourceManager resourceManager, @NonNull E instance, @NonNull Class<E> type) {
			super(resourceManager, instance, type);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void preManage() {
			// TODO Auto-generated method stub
			ResourceDescriptor<E> descriptor = getResourceDescriptor();
			// @formatter:off
			if (descriptor.getIdentifierValueGeneration().getGenerationTiming().equals(GenerationTiming.ALWAYS)) {
				getResourceDescriptor().setIdentifier(instance,
						descriptor.getIdentifierValueGeneration()
							.getValueGenerator()
							.generateValue(getResourceManager().getResourceManagerFactory(), instance));
			}
			// @formatter:on
		}

	}

}
