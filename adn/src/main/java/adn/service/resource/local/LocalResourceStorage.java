/**
 * 
 */
package adn.service.resource.local;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorage extends Service {

	boolean isExists(String filename);

}
