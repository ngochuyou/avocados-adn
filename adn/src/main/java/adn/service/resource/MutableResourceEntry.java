/**
 * 
 */
package adn.service.resource;

import java.io.Serializable;

import javax.persistence.LockModeType;

import adn.service.resource.persister.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public class MutableResourceEntry extends AbstractResourceEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param status
	 * @param loadedState
	 * @param rowId
	 * @param id
	 * @param version
	 * @param lockMode
	 * @param existsInStorage
	 * @param persister
	 * @param disableVersionIncrement
	 * @param persistenceContext
	 */
	public MutableResourceEntry(Status status, Object[] loadedState, Object rowId, Serializable id, Object version,
			LockModeType lockMode, boolean existsInStorage, ResourcePersister persister,
			boolean disableVersionIncrement) {
		super(status, loadedState, rowId, id, version, lockMode, existsInStorage, persister, disableVersionIncrement);
		// TODO Auto-generated constructor stub
	}

}