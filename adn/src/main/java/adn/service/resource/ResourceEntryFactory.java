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
public interface ResourceEntryFactory extends Serializable {

	ResourceEntry createEntityEntry(final Status status, final Object[] loadedState, final Object rowId,
			final Serializable id, final Object version, final LockModeType lockMode, final boolean existsInDatabase,
			final ResourcePersister persister, final boolean disableVersionIncrement,
			final PersistenceContext persistenceContext);

}
