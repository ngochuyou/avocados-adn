/**
 * 
 */
package adn.service.resource.local;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.internal.EntityState;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceEntry<T> extends EntityEntry {

	@Override
	@Deprecated
	default void overwriteLoadedStateCollectionValue(String propertyName, PersistentCollection collection) {
		// TODO Auto-generated method stub
		unsupport();
	}

	@Override
	@Deprecated
	default EntityPersister getPersister() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default boolean isNullifiable(boolean earlyInsert, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	@Deprecated
	default EntityKey getEntityKey() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	ResourceDescriptor<T> getDescriptor();

	public static <T> EntityState getEntityState(ResourceEntry<T> entry, T resource, ResourceDescriptor<T> descriptor) {
		if (entry != null) {
			// entering this logic determine that resource is either DELETED or PERSISTENT
			if (entry.getStatus() == Status.DELETED) {
				return EntityState.DELETED;
			}

			return EntityState.PERSISTENT;
		}
		// either TRANSIENT or DETACHED
		// descriptor
		return descriptor.isTransient(resource) ? EntityState.TRANSIENT : EntityState.DETACHED;
	}

	ResourceKey<T> getResourceKey();

}
