/**
 * 
 */
package adn.service.resource.local;

import static adn.service.resource.local.ResourceManagerFactoryBuilder.unsupport;

import org.hibernate.EntityMode;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.type.Type;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister<T> extends EntityPersister {

	@Override
	@Deprecated
	default boolean isLazyPropertiesCacheable() {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	default boolean canReadFromCache() {
		// caching is currently not supported
		return false;
	}

	@Override
	@Deprecated
	default CacheEntry buildCacheEntry(Object entity, Object[] state, Object version,
			SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default boolean isCacheInvalidationRequired() {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	@Deprecated
	default boolean canUseReferenceCacheEntries() {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	@Deprecated
	default boolean hasCache() {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	default boolean canWriteToCache() {
		return false;
	}

	@Override
	@Deprecated
	default EntityDataAccess getCacheAccessStrategy() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default CacheEntryStructure getCacheEntryStructure() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default NaturalIdDataAccess getNaturalIdCacheAccessStrategy() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default boolean hasNaturalIdCache() {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	default EntityMode getEntityMode() {
		// TODO Auto-generated method stub
		return EntityMode.POJO;
	}

	@Override
	@Deprecated
	default public NavigableRole getNavigableRole() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	<E> E unwrap(Class<E> type);

	PropertyAccess getPropertyAccess(String propertyName);

	PropertyAccess getPropertyAccess(int propertyIndex);

	PropertyAccess locatePropertyAccess(String propertyName);

	ValueGeneration getValueGeneration(String propertyName);

	ValueGeneration getValueGeneration(int propertyIndex);

	ValueGeneration locateValueGeneration(String propertyName);

	Type locatePropertyType(String properyName);

	String[] getPropertyColumnNames(String propertyName);

	int getPropertyIndex(String propertyName);

}
