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

	<T> T select(Serializable identifier);
	
	<T> List<T> select(Serializable[] identifier);

	void lock(Serializable identifier);
	
}
