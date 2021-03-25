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

	Object find(String pathName);

	void add(Serializable pathName, Object resource);

	void remove(String pathName);

	void clear();

	boolean contains(String pathName);

	ResourceManager getResourceManager();

}
