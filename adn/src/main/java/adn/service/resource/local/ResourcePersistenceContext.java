/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersistenceContext {

	Serializable getId();

	Object find(String pathName);

	void add(Object resource);

	void remove(String pathName);

	void commit();

	void clear();

	default void close() {
		getResourceManager().doResourceContextClose(this);
	}

	boolean contains(String pathName);

	ResourceManager getResourceManager();

}
