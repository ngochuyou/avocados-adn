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
public class ImmutableResourceEntry extends AbstractResourceEntry {

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
	public ImmutableResourceEntry(final Status status, final Object[] loadedState, final Object rowId,
			final Serializable id, final Object version, final LockModeType lockMode, final boolean existsInStorage,
			final ResourcePersister persister, final boolean disableVersionIncrement) {
		super(status, loadedState, rowId, id, version, lockMode, existsInStorage, persister, disableVersionIncrement);
		// TODO Auto-generated constructor stub
	}

}