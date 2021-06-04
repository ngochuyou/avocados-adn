/**
 * 
 */
package adn.service.resource.engine;

import java.io.Serializable;
import java.sql.Statement;

/**
 * @author Ngoc Huy
 *
 */
public class ExceptionResultSet extends ResourceResultSet {

	public ExceptionResultSet(RuntimeException re, Statement statement) {
		super(new Serializable[][] { new Serializable[] { re.getClass(), re.getMessage() } }, new ExceptionResultSetMetadata(), statement);
	}

	public static class ExceptionResultSetMetadata extends ResultSetMetaDataImpl {

		private ExceptionResultSetMetadata() {
			super(null, new String[] { "type", "message" });
		}

	}

}
