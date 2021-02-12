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
public class MutableResourceEntryFactory implements ResourceEntryFactory {

	public static MutableResourceEntryFactory INSTANCE = new MutableResourceEntryFactory();

	/**
	 * 
	 */
	private MutableResourceEntryFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public ResourceEntry createEntityEntry(Status status, Object[] loadedState, Object rowId, Serializable id,
			Object version, LockModeType lockMode, boolean existsInDatabase, ResourcePersister persister,
			boolean disableVersionIncrement, PersistenceContext persistenceContext) {
		// TODO Auto-generated method stub
		return new MutableResourceEntry(status, loadedState, rowId, id, version, lockMode, existsInDatabase, persister,
				disableVersionIncrement);
	}

}
