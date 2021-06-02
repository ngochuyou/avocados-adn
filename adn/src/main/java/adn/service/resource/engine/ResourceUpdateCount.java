/**
 * 
 */
package adn.service.resource.engine;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceUpdateCount extends ResourceResultSet {

	ResourceUpdateCount(Integer[] updateCounts, String tablename) {
		super(new Integer[][] { updateCounts }, new ResourceUpdateCountMetadata(tablename));
	}

	public static class ResourceUpdateCountMetadata extends ResultSetMetaDataImpl {

		private ResourceUpdateCountMetadata(String tableName) {
			super(tableName, new String[] { "0" });
		}

	}

}
