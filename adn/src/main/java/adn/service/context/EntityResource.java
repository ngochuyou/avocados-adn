/**
 * 
 */
package adn.service.context;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class EntityResource<T extends Entity> extends Resource {

	private T instance;

	/**
	 * @param id
	 */
	public EntityResource(T instance) {
		super(instance.getId().toString());
		// TODO Auto-generated constructor stub
		this.instance = instance;
	}

	public T getInstance() {
		return instance;
	}

	public void setInstance(T instance) {
		this.instance = instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	Class<T> getResourceType() {
		// TODO Auto-generated method stub
		return (Class<T>) this.instance.getClass();
	}

}
