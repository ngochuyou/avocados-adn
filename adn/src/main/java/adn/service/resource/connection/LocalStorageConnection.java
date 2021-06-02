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

	interface Settings {
	
		public static final int BATCH_SIZE = 500;
		public static final int MAX_RESULT_SET_ROWS = 1000;
		public static final int MAX_FIELD_SIZE = Integer.MAX_VALUE;
		
	}

	LocalStorage getStorage();

}
