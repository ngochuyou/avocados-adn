/**
 * 
 */
package adn.service.resource.connection;

import java.sql.Connection;

import adn.service.resource.engine.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorageConnection extends Connection {

	LocalResourceStorage getStorage();

}
