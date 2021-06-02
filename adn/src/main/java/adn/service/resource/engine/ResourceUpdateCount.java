/**
 * 
 */
package adn.service.resource.engine;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceUpdateCount extends ResourceResultSet {

	ResourceUpdateCount(int[] updateCounts, String tablename) {
		super(IntStream.range(0, updateCounts.length).mapToObj(index -> new Object[] { updateCounts[index] })
				.collect(Collectors.toList()), new ResourceUpdateCountMetadata(tablename));
	}

	public static class ResourceUpdateCountMetadata extends ResultSetMetaDataImpl {

		private ResourceUpdateCountMetadata(String tableName) {
			super(tableName, new String[] { "0" });
		}

	}

}
