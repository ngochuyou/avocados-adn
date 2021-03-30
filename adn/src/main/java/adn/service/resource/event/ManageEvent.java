/**
 * 
 */
package adn.service.resource.event;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public class ManageEvent<T> extends AbstractPersistentEvent<T> {

	private static final long serialVersionUID = 1L;

	private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

	public ManageEvent(ResourceManager resourceManager, T instance, Class<T> type) {
		// TODO Auto-generated constructor stub
		super(resourceManager, instance, type);
	}

	@Override
	public void preFire() {
		// TODO Auto-generated method stub
	}

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		try {
			preFire();

			Serializable identifier;

			getResourceContext().add(identifier = getResourceDescriptor().getIdentifier(instance), instance);
			logger.trace("Managing resource of type: " + instance.getClass() + "identifier: " + identifier);
		} catch (Exception e) {
			logger.error("Failed to fire event " + this.getClass() + ". Message: " + e.getMessage());
		}
	}

}
