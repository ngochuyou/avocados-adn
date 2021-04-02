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

	<T> void manage(T instance, Class<T> type);

	<T> T find(Serializable identifier, Class<T> type);

	<T> Serializable save(T instance);

	ResourceManagerFactory getResourceManagerFactory();

	void setRollbackOnly();

	boolean isRollbackOnly();

	ResourceContext getResourceContext();

	ActionQueue getActionQueue();
	
}
