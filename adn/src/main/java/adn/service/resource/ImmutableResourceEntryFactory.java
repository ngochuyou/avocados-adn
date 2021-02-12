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
public class ImmutableResourceEntryFactory implements ResourceEntryFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static ImmutableResourceEntryFactory INSTANCE = new ImmutableResourceEntryFactory();

	/**
	 * 
	 */
	private ImmutableResourceEntryFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ResourceEntry createEntityEntry(Status status, Object[] loadedState, Object rowId, Serializable id,
			Object version, LockModeType lockMode, boolean existsInDatabase, ResourcePersister persister,
			boolean disableVersionIncrement, PersistenceContext persistenceContext) {
		// TODO Auto-generated method stub
		return new ImmutableResourceEntry(status, loadedState, rowId, id, version, lockMode, existsInDatabase,
				persister, disableVersionIncrement);
	}

}
