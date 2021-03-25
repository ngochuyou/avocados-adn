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

	void add(Serializable id, Object resource);

	void remove(String pathName);

	void commit();

	void clear();

	default void close() {
		getResourceManager().doContextClose(this);
	}

	boolean contains(String pathName);

	ResourceManager getResourceManager();

}
