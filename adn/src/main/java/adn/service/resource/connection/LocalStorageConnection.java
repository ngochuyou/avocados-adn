/**
 * 
 */
package adn.service.resource.connection;

import java.sql.Connection;

import adn.service.resource.engine.LocalStorage;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalStorageConnection extends Connection {

	void registerTemplate(ResourceTemplate template);

	LocalStorage getStorage();

}
