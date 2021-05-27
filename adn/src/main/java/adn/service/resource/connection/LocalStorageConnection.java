/**
 * 
 */
package adn.service.resource.connection;

import java.sql.Connection;

import adn.service.resource.engine.LocalStorage;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalStorageConnection extends Connection {

	LocalStorage getStorage();

}
