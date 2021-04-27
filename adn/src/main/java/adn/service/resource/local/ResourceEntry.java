/**
 * 
 */
package adn.service.resource.local;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.internal.EntityState;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceEntry<T> extends EntityEntry {

	@Override
	@Deprecated
	default void overwriteLoadedStateCollectionValue(String propertyName, PersistentCollection collection) {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
	}

	@Override
	ResourcePersister<T> getPersister();

	@Override
	@Deprecated
	default boolean isNullifiable(boolean earlyInsert, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return false;
	}

	public static <T> EntityState getEntityState(ResourceEntry<T> entry, T resource, ResourcePersister<T> descriptor) {
		if (entry != null) {
			// entering this logic determine that resource is either DELETED or PERSISTENT
			if (entry.getStatus() == Status.DELETED) {
				return EntityState.DELETED;
			}

			return EntityState.PERSISTENT;
		}
		// either TRANSIENT or DETACHED
		// descriptor
//		return descriptor.isTransient(resource) ? EntityState.TRANSIENT : EntityState.DETACHED;
		return null;
	}

}
