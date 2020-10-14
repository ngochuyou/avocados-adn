/**
 * 
 */
package adn.service.context;

import adn.service.context.transaction.Transaction;

/**
 * Central <i>ContextManager</i> for this application<br>
 * Implementations of this interface should perform following managements:
 * <ul>
 * <li>Resource persistence</li>
 * <li>Transactions</li>
 * <li>Events</li>
 * </ul>
 * 
 * @author Ngoc Huy
 *
 */
public interface ServiceManager {

	/**
	 * Put the resource into <i>Context</i>, the state of this instance is the
	 * initial state
	 * 
	 * @param o
	 */
	void persist(Object o);

	/**
	 * Detach the resource out of <i>Context</i>, actions related to this resource
	 * should be managed properly
	 * 
	 * @param o
	 */
	void detach(Object o);

	/**
	 * Get the persisted resource
	 * 
	 * @param id resource id
	 * @return the resource persisted by this <i>Context</i>
	 */
	Object load(Object id);

	/**
	 * Perform the lock strategy onto an object
	 * 
	 * @param id   identifier of the targeted resource
	 * @param mode specified lock mode
	 */
	void lock(Object id, LockMode mode);

	/**
	 * Determines whether the resource exists in the <i>Context</i>
	 * 
	 * @param o
	 * @return true if the instance is persisted
	 */
	boolean contains(Object o);

	/**
	 * Open a transaction to manage the resource
	 * 
	 * @return the new {@link Transaction} instance
	 */
	Transaction openTransaction();

	/**
	 * This method must provide a globally managed identifier for the {@link Resource}(es)
	 *  
	 * @return 
	 */
	Object generateResourceId();
	
	/**
	 * Resource common lock mode
	 * 
	 * @author Ngoc Huy
	 *
	 */
	enum LockMode {
		READ, WRITE
	}

}
