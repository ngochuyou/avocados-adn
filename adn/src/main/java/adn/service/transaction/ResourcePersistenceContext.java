/**
 * 
 */
package adn.service.transaction;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
@SuppressWarnings("unused")
public class ResourcePersistenceContext {

	private final int COLLECTION_SIZE = 80;

	private Map<ResourceKey, Persistence> entities = new HashMap<>(COLLECTION_SIZE);

	private Map<ResourceKey, Persistence> deletedEntities = new HashMap<>(COLLECTION_SIZE);

	private Map<Persistence, ResourceEntry> entryMap = new IdentityHashMap<>();
	
	
	
}
