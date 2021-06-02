/**
 * 
 */
package adn.service.resource.engine;

import java.util.Arrays;

/**
 * @author Ngoc Huy
 *
 */
public class ExceptionResultSet extends ResourceResultSet {

	public ExceptionResultSet(RuntimeException re) {
		super(Arrays.asList(new Object[] { re.getClass(), re.getMessage() }), new ExceptionResultSetMetadata());
	}

	public static class ExceptionResultSetMetadata extends ResultSetMetaDataImpl {

		private ExceptionResultSetMetadata() {
			super(null, new String[] { "type", "message" });
		}

	}

}
