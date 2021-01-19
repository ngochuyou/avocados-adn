/**
 * 
 */
package adn.service.resource.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.hibernate.engine.spi.Status;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public final class ResourcePersistenceContext {

	private final EntityManager entityManager;

	private final int COLLECTION_SIZE = 100;

	private final String ENTRY_NOT_FOUND = "Entry not found";

	private volatile Map<ResourceKey, Object> resources = new ConcurrentHashMap<>(COLLECTION_SIZE);

	private volatile Map<ResourceKey, Object> deletedResources = new ConcurrentHashMap<>(COLLECTION_SIZE);

	private volatile Map<ResourceKey, ResourceEntry> entryMap = new ConcurrentHashMap<>(COLLECTION_SIZE);

	public ResourcePersistenceContext(EntityManager entityManager) {
		super();
		Assert.notNull(entityManager, "EntityManager can not be null");
		this.entityManager = entityManager;
	}

	public void addResource(ResourceKey key, Object o) {
		resources.put(key, o);
	}

	public ResourceEntry addResource(Object resource, Status status, Object state, ResourceKey key, Object version,
			LockModeType lockMode) {
		addResource(key, resource);

		return addEntry(key, status, state, key.getIdentifier(), version, lockMode, resource.getClass());
	}

	public ResourceEntry addEntry(ResourceKey key, Status status, Object loadedState, Serializable identifier,
			Object version, LockModeType lockMode, Type type) {
		ResourceEntryImpl entry = new ResourceEntryImpl(key.getIdentifier(), key, lockMode, status, type, this);

		entryMap.put(key, entry);

		return entry;
	}

	public Object getResource(ResourceKey key) {

		return resources.get(key);
	}

	public ResourceEntry getEntry(ResourceKey key) {

		return entryMap.get(key);
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

	public ResourceEntry removeEntry(ResourceKey key) {
		ResourceEntry entry = entryMap.get(key);

		if (entry == null) {
			return null;
		}

		entryMap.remove(key);

		return entry;
	}

	public boolean hasEntry(Object resource) {

		return entryMap.get(resource) != null;
	}

	public void setEntryStatus(ResourceKey key, Status status) {
		ResourceEntry entry = entryMap.get(key);

		Assert.notNull(entry, ENTRY_NOT_FOUND);
		entryMap.compute(key, (k, v) -> {
			v.setStatus(status);

			return v;
		});
	}

	public void setEntryLockMode(ResourceKey key, LockModeType lockMode) {
		ResourceEntry entry = entryMap.get(key);

		Assert.notNull(entry, ENTRY_NOT_FOUND);
		entryMap.compute(key, (k, v) -> {
			v.setLockMode(lockMode);

			return v;
		});
	}

	public void clear() {
		resources.clear();
		deletedResources.clear();
		entryMap.clear();
	}

	public EntityManager getEntityManager() {

		return entityManager;
	}

}
