/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.LockMode;
import org.hibernate.engine.internal.MutableEntityEntryFactory;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityEntryFactory;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResourceEntryFactory implements EntityEntryFactory {

	public static final ResourceEntryFactory INSTANCE = new ResourceEntryFactory(MutableEntityEntryFactory.INSTANCE);

	private final MutableEntityEntryFactory mutableEntryFactory;

	private ResourceEntryFactory(MutableEntityEntryFactory mutableEntryFactory) {
		// TODO Auto-generated constructor stub
		this.mutableEntryFactory = mutableEntryFactory;
	}

	@Override
	public ResourceEntry<?> createEntityEntry(Status status, Object[] loadedState, Object rowId, Serializable id,
			Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister,
			boolean disableVersionIncrement, PersistenceContext persistenceContext) {
		// TODO Auto-generated method stub
		EntityEntry entityEntry = mutableEntryFactory.createEntityEntry(status, loadedState, rowId, id, version,
				lockMode, existsInDatabase, persister, disableVersionIncrement, persistenceContext);

		return new ResourceEntryImpl<>(entityEntry, persistenceContext, null);
	}

}
