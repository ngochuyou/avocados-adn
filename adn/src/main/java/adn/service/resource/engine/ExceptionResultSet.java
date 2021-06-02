/**
 * 
 */
package adn.service.resource.engine;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public class ExceptionResultSet extends ResourceResultSet {

	public ExceptionResultSet(RuntimeException re) {
		super(new Serializable[][] { new Serializable[] { re.getClass(), re.getMessage() } }, new ExceptionResultSetMetadata());
	}

	public static class ExceptionResultSetMetadata extends ResultSetMetaDataImpl {

		private ExceptionResultSetMetadata() {
			super(null, new String[] { "type", "message" });
		}

	}

}
