/**
 * 
 */
package adn.service.resource.engine;

import java.sql.Statement;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceUpdateCount extends ResourceResultSet {

	ResourceUpdateCount(Long[] updateCounts, String tablename, Statement statement) {
		super(new Long[][] { updateCounts }, new ResourceUpdateCountMetadata(tablename), statement);
	}

	public static class ResourceUpdateCountMetadata extends ResultSetMetaDataImpl {

		private ResourceUpdateCountMetadata(String tableName) {
			super(tableName, new String[] { "0" });
		}

	}

}
