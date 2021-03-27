/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceContext {

	Object find(Serializable identifier);

	void add(Serializable identifier, Object resource);

	void remove(Serializable identifier);

	void clear();

	boolean contains(Serializable pathName);

	ResourceManager getResourceManager();

}
