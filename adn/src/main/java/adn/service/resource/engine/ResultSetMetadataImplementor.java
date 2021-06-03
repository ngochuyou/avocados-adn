/**
 * 
 */
package adn.service.resource.engine;

import java.sql.ResultSetMetaData;

/**
 * @author Ngoc Huy
 *
 */
public interface ResultSetMetadataImplementor extends ResultSetMetaData {

	String[] getActualColumnNames();

	Integer getColumnIndexFromActualName(String columnName);

	Integer getColumnIndexfromAlias(String columnName);

}
