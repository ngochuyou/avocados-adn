/**
 * 
 */
package adn.service.resource.local;

import static adn.service.resource.local.ResourceManagerFactory.unsupportHBN;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceEntry<T> extends EntityEntry {

	@Override
	default void overwriteLoadedStateCollectionValue(String propertyName, PersistentCollection collection) {
		// TODO Auto-generated method stub
		unsupportHBN();
	}

	@Override
	default EntityPersister getPersister() {
		// TODO Auto-generated method stub
		unsupportHBN();
		return null;
	}

	ResourceDescriptor<T> getDescriptor();

	@Override
	default boolean isNullifiable(boolean earlyInsert, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupportHBN();
		return false;
	}

}
