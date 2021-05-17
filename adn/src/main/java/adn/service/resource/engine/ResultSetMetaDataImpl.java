/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class ResultSetMetaDataImpl implements ResultSetMetaData {

	private final String tableName;

	private final Map<String, Integer> columnMap = new HashMap<>();
	private final Map<Integer, String> indexMap = new HashMap<>();

	ResultSetMetaDataImpl(String tableName, String[] columns) {
		super();
		this.tableName = tableName;

		for (int i = 0; i < columns.length; i++) {
			columnMap.put(columns[i], i);
			indexMap.put(i, columns[i]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (isWrapperFor(iface)) {
			return (T) this;
		}

		throw new ClassCastException("Unable to unwrap to " + iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isAssignableFrom(this.getClass());
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columnMap.size();
	}

	private void assertIndex(int index) throws IllegalArgumentException, SQLException {
		Assert.isTrue(index >= 0 && index < getColumnCount(), "Index " + index + " is out of bound");
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		assertIndex(column);
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		assertIndex(column);
		return true;
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		assertIndex(column);
		return true;
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		return false;
	}

	@Override
	public int isNullable(int column) throws SQLException {
		assertIndex(column);
		return 0;
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		assertIndex(column);
		return true;
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		assertIndex(column);
		return Integer.MAX_VALUE;
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		return getColumnName(column);
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		assertIndex(column);
		return indexMap.get(column);
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		return File.class.getName();
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		return tableName;
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		return tableName;
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		return null;
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		return false;
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		return !isReadOnly(column);
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return !isReadOnly(column);
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		return getColumnName(column);
	}

	public int getIndex(String name) {
		return columnMap.get(name);
	}

}
