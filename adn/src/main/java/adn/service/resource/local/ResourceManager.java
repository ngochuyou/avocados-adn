/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManager {

	ResourcePersistenceContext getContext(Serializable identifier)
			throws IllegalStateException, IllegalArgumentException;

	ResourcePersistenceContext openContext();

	<T> ResourceTuplizer<T> getTuplizer(String name);

	<T> ResourceTuplizer<T> getTuplizer(Class<T> clazz);
	
	void doResourceContextClose(ResourcePersistenceContext context);

	boolean isLocked(Serializable identifier);

	boolean setLocked(Serializable identifier, boolean isLocked);

}
