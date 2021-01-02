/**
 * 
 */
package adn.service.transaction;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
@SuppressWarnings("unused")
public class ResourcePersistenceContext {

	private final EntityManager entityManager;

	private final int COLLECTION_SIZE = 80;

	private Map<ResourceKey, Object> entities = new HashMap<>(COLLECTION_SIZE);

	private Map<ResourceKey, Object> deletedEntities = new HashMap<>(COLLECTION_SIZE);

	private Map<Object, ResourceEntry> entryMap = new IdentityHashMap<>();

	public ResourcePersistenceContext(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}

	public void addResource(ResourceKey key, Object o) {
		entities.put(key, o);
	}

}
