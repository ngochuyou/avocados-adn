/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.LockMode;
import org.hibernate.engine.internal.AbstractEntityEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.Status;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceEntryImpl<T> extends AbstractEntityEntry implements ResourceEntry<T> {

	private static final long serialVersionUID = 1L;

	private final transient ResourcePersister<T> persister;

	private final EntityKey key;

	// @formatter:off
	private ResourceEntryImpl(
			SessionFactoryImplementor factory,
			String entityName,
			Serializable id, Status status,
			Status previousStatus,
			Object[] loadedState,
			Object[] deletedState,
			Object version,
			LockMode lockMode,
			boolean existsInDatabase,
			boolean isBeingReplicated,
			PersistenceContext persistenceContext,
			ResourcePersister<T> persister,
			EntityKey key) {
		super(factory, entityName, id, status, previousStatus, loadedState, deletedState, version, lockMode,
				existsInDatabase, isBeingReplicated, persistenceContext);
		this.persister = persister;
		this.key = key;
	}
	
	public ResourceEntryImpl(
			String entityName,
			Serializable id,
			Status status,
			Status previousStatus,
			Object[] loadedState,
			Object[] deletedState,
			Object version,
			LockMode lockMode,
			boolean existsInDatabase,
			PersistenceContext context,
			ResourcePersister<T> persister,
			EntityKey key) {
		// TODO Auto-generated constructor stub
		this(
			null,
			entityName,
			id,
			status,
			previousStatus,
			loadedState,
			deletedState,
			version,
			lockMode,	
			existsInDatabase,
			false,
			context,
			persister,
			key);
	}

	@SuppressWarnings("unchecked")
	public ResourceEntryImpl(EntityEntry entry, PersistenceContext context, Status previousState) {
		this(
			null,
			entry.getEntityName(),
			entry.getId(),
			entry.getStatus(),
			previousState,
			entry.getLoadedState(),
			entry.getDeletedState(),
			entry.getVersion(),
			entry.getLockMode(),	
			entry.isExistsInDatabase(),
			false,
			context,
			(ResourcePersister<T>) entry.getPersister(),
			entry.getEntityKey()
				);
	}
	// @formatter:on

	@Override
	public ResourcePersister<T> getPersister() {
		// TODO Auto-generated method stub
		return persister;
	}

	@Override
	public EntityKey getEntityKey() {
		// TODO Auto-generated method stub
		return key;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("[entityName: %s|"
				+ "id: %s|"
				+ "status: %s|"
				+ "loadedState: %s|"
				+ "deletedState: %s|"
				+ "version: %s|"
				+ "lockMode: %s|"
				+ "entityKey: %s"
				+ "]",
				getEntityName(),
				getEntityKey().getIdentifier(),
				getStatus(),
				getLoadedState() != null ? "[" + Stream.of(getLoadedState()).map(val -> val.toString()).collect(Collectors.joining(", ")) + "]" : "NULL",
				getDeletedState() != null ? "[" + Stream.of(getDeletedState()).map(val -> val.toString()).collect(Collectors.joining(", ")) + "]" : "NULL",
				getVersion().toString(),
				getLockMode().toString(),
				getEntityKey().toString());
		// @formatter:on
	}

}
