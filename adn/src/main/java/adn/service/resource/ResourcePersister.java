/**
 * 
 */
package adn.service.resource;

import org.hibernate.EntityMode;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.PropertyAccess;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister<T> extends EntityPersister {

	@Override
	default boolean canWriteToCache() {
		return false;
	}

	@Override
	default NaturalIdDataAccess getNaturalIdCacheAccessStrategy() {
		return null;
	}

	@Override
	default boolean hasNaturalIdCache() {
		return false;
	}

	@Override
	default EntityMode getEntityMode() {
		// TODO Auto-generated method stub
		return EntityMode.POJO;
	}

	@SuppressWarnings("unchecked")
	default <E> E unwrap(Class<E> type) {
		return (E) this;
	}

	PropertyAccess getPropertyAccess(String propertyName);

	PropertyAccess getPropertyAccess(int propertyIndex);

}
