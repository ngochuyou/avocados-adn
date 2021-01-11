/**
 * 
 */
package adn.service.resource.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.hibernate.engine.spi.Status;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
@Component
@SuppressWarnings("unused")
public final class ResourcePersistenceContext {

	private final EntityManager entityManager;

	private final int COLLECTION_SIZE = 100;

	private final String ENTRY_NOT_FOUND = "Entry not found";
	
	private volatile Map<ResourceKey, Object> resources = new HashMap<>(COLLECTION_SIZE);

	private volatile Map<ResourceKey, Object> deletedResources = new HashMap<>(COLLECTION_SIZE);

	private volatile Map<Object, ResourceEntry> entryMap = new IdentityHashMap<>();

	public ResourcePersistenceContext(EntityManager entityManager) {
		super();
		Assert.notNull(entityManager, "EntityManager can not be null");
		this.entityManager = entityManager;
	}

	public void addResource(ResourceKey key, Object o) {
		resources.put(key, o);
	}

	public ResourceEntry addResource(Object resource, Status status, Object state, ResourceKey key, Object version,
			LockModeType lockMode, Type type) {
		addResource(key, resource);

		return addEntry(resource, status, state, key.getIdentifier(), version, lockMode, type);
	}

	public ResourceEntry addEntry(Object resource, Status status, Object loadedState, Serializable identifier,
			Object version, LockModeType lockMode, Type type) {
		ResourceEntryImpl entry = new ResourceEntryImpl(identifier, lockMode, status, type);

		entryMap.put(resource, entry);

		return entry;
	}

	public Object getResource(ResourceKey key) {

		return resources.get(key);
	}

	public ResourceEntry getEntry(Object resource) {

		return entryMap.get(resource);
	}

	public Object removeResource(ResourceKey key) {
		Object resource = resources.get(key);

		if (resource == null) {
			return null;
		}

		deletedResources.put(key, resource);
		resources.remove(key);

		ResourceEntry entry = entryMap.get(resource);

		Assert.notNull(entry, ENTRY_NOT_FOUND);
		entryMap.compute(key, (k, v) -> {
			v.setStatus(Status.DELETED);

			return v;
		});

		return resource;
	}

	public ResourceEntry removeEntry(Object resource) {
		ResourceEntry entry = entryMap.get(resource);

		if (entry == null) {
			return null;
		}

		entryMap.remove(entry);

		return entry;
	}

	public boolean hasEntry(Object resource) {

		return entryMap.get(resource) != null;
	}
	
	public void setEntryStatus(Object resource, Status status) {
		ResourceEntry entry = entryMap.get(resource);
		
		Assert.notNull(entry, ENTRY_NOT_FOUND);
		entryMap.compute(resource, (k, v) -> {
			v.setStatus(status);
			
			return v;
		});
	}
	
	public void setEntryLockMode(Object resource, LockModeType lockMode) {
		ResourceEntry entry = entryMap.get(resource);
		
		Assert.notNull(entry, ENTRY_NOT_FOUND);
		entryMap.compute(resource, (k, v) -> {
			v.setLockMode(lockMode);
			
			return v;
		});
	}
	
	public void clear() {
		resources.clear();
		deletedResources.clear();
		entryMap.clear();
	}

}
