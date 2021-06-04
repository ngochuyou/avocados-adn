/**
 * 
 */
package adn.service.resource.connection;

import java.sql.Connection;

import adn.application.context.ContextProvider;
import adn.service.resource.engine.LocalStorage;

/**
 * @author Ngoc Huy
 *
 */
public class ConnectionBuilder {

	public static final ConnectionBuilder INSTANCE = new ConnectionBuilder();

	private ConnectionBuilder() {}

	public Connection createConnection() {
		return new ConnectionImpl(ContextProvider.getApplicationContext().getBean(LocalStorage.class));
	}

}