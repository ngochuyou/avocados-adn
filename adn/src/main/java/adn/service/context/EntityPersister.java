/**
 * 
 */
package adn.service.context;

import javax.persistence.PersistenceException;

import org.springframework.stereotype.Component;

import adn.model.entities.Entity;

/**
 * This persist strategy produce a wrapper for any instance of type
 * {@link Entity}
 * 
 * @author Ngoc Huy
 *
 */
@Component("entityPersister")
public class EntityPersister implements ResourcePersister {
	
	@Override
	public boolean supports(Object o) {
		// TODO Auto-generated method stub
		return o instanceof Entity;
	}
	
	@Override
	public EntityResource<? extends Entity> persist(Object o) {
		// TODO Auto-generated method stub
		if (!(o instanceof Entity)) {
			throw new PersistenceException("Cannot persist instance. Instance is not an " + Entity.class);
		}

		return new EntityResource<>((Entity) o);
	}

}
