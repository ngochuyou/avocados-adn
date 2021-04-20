/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.LockMode;
import org.hibernate.engine.internal.AbstractEntityEntry;
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

	private final ResourceKey<T> key;

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
			ResourceKey<T> key) {
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
			ResourceContext context,
			ResourcePersister<T> persister,
			ResourceKey<T> key) {
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
	// @formatter:on

	@Override
	public ResourcePersister<T> getPersister() {
		// TODO Auto-generated method stub
		return persister;
	}

	@Override
	public ResourceKey<T> getResourceKey() {
		// TODO Auto-generated method stub
		return key;
	}

}
