/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorage extends Service {

	boolean isExists(String filename);

	<T> T select(Serializable identifier);

}
