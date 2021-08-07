/**
 * 
 */
package adn.service.resource.connection;

import java.sql.Connection;

import adn.service.resource.engine.Storage;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalStorageConnection extends Connection {

	interface Settings {

		public static final int MAX_RESULT_SET_ROWS = 1000;
		public static final int MAX_FIELD_SIZE = Integer.MAX_VALUE;

	}

	Storage getStorage();

}
