/**
 * 
 */
package adn.service.resource.storage;

import java.io.Serializable;
import java.util.List;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorage extends Service {

	boolean isExists(String filename);

	Object select(Serializable identifier);

	List<Object> select(Serializable[] identifier);

	void lock(Serializable identifier);

}
