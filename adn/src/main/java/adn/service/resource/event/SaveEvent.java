/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public abstract class SaveEvent<T> extends AbstractPersistentEvent<T> {

	private static final long serialVersionUID = 1L;

	public SaveEvent(ResourceManager resourceManager, T instance, Class<T> type) {
		// TODO Auto-generated constructor stub
		super(resourceManager, instance, type);
	}

	abstract void postSave();

}
