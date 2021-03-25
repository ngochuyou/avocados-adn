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

	ResourceManagerFactory getResourceManagerFactory();

}
