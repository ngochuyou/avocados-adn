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

	private final transient ResourceDescriptor<T> descriptor;

	private ResourceEntryImpl(SessionFactoryImplementor factory, String entityName, Serializable id, Status status,
			Status previousStatus, Object[] loadedState, Object[] deletedState, Object version, LockMode lockMode,
			boolean existsInDatabase, boolean isBeingReplicated, PersistenceContext persistenceContext,
			ResourceDescriptor<T> descriptor) {
		super(factory, entityName, id, status, previousStatus, loadedState, deletedState, version, lockMode,
				existsInDatabase, isBeingReplicated, persistenceContext);
		this.descriptor = descriptor;
	}

	// @formatter:off
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
			ResourceDescriptor<T> descriptor) {
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
			null,
			descriptor);
	}
	// @formatter:on

	@Override
	public ResourceDescriptor<T> getDescriptor() {
		// TODO Auto-generated method stub
		return descriptor;
	}

}
