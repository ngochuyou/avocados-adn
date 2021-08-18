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

	public ExceptionResultSet(Exception re, Statement statement) {
		super(new Serializable[][] { new Serializable[] { 0, re.getClass(), re.getMessage() } },
				ExceptionResultSetMetadata.INSTANCE, statement);
	}

	public ExceptionResultSet(Statement statement) {
		super(new Serializable[][] {
				new Serializable[] { 0, RuntimeException.class, "Encountered an unknown exception" } },
				ExceptionResultSetMetadata.INSTANCE, statement);
	}

	public static class ExceptionResultSetMetadata extends ResultSetMetaDataImpl {

		private static final ExceptionResultSetMetadata INSTANCE = new ExceptionResultSetMetadata();

		private ExceptionResultSetMetadata() {
			super(null, new String[] { "modCount", "type", "message" });
		}

	}

}
