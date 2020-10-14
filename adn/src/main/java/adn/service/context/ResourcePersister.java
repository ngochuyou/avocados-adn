/**
 * 
 */
package adn.service.context;

import javax.persistence.PersistenceException;

/**
 * Determine the persist strategy applied on a application resource
 * 
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister {

	/**
	 * Persist the object passed into
	 * 
	 * @param o the targeted object
	 * @return the persisted {@link Resource}
	 */
	Resource persist(Object o) throws PersistenceException;

	/**
	 * @param o
	 * @return true if persisting strategy supports object type
	 */
	boolean supports(Object o);
	
}
