/**
 * 
 */
package adn.service.resource.engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import adn.service.resource.connection.ConnectionImpl;
import adn.service.resource.connection.LocalStorageConnection;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;

/**
 * @author Ngoc Huy
 *
 */
public class StatementImpl implements Statement {

	private final LocalStorageConnection connection;

	private volatile boolean isClosed = false;
	protected int timeout = ConnectionImpl.DEFAULT_QUERY_TIMEOUT;

	public static final int DEFAULT_FETCH_SIZE = 500;
	private int fetchSize = DEFAULT_FETCH_SIZE;
	private int maxFieldSize = ConnectionImpl.MAX_FIELD_SIZE;
	private int resultSetMaxRows = ConnectionImpl.RESULT_SET_MAX_ROWS;

	protected List<Query> batchList = new ArrayList<>();
	protected List<ResultSetImplementor> results;

	public StatementImpl(LocalStorageConnection connection) {
		this.connection = connection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T) iface;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isAssignableFrom(this.getClass());
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		return getConnection().getStorage().query(QueryCompiler.compile(sql));
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return 0;
	}

	protected void checkClose() throws SQLException {
		if (isClosed) {
			throw new SQLException("Statement was closed");
		}
	}

	@Override
	public synchronized void close() throws SQLException {
		this.isClosed = true;
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return maxFieldSize;
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		this.maxFieldSize = max;
	}

	@Override
	public int getMaxRows() throws SQLException {
		return resultSetMaxRows;
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		this.resultSetMaxRows = max > ConnectionImpl.RESULT_SET_MAX_ROWS ? ConnectionImpl.RESULT_SET_MAX_ROWS : max;
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {}

	@Override
	public int getQueryTimeout() throws SQLException {
		return timeout;
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		this.timeout = seconds;
	}

	@Override
	public void cancel() throws SQLException {}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {}

	@Override
	public void setCursorName(String name) throws SQLException {}

	@Override
	public boolean execute(String sql) throws SQLException {
		return false;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return null;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		return false;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {

	}

	@Override
	public int getFetchDirection() throws SQLException {
		return ResultSet.FETCH_FORWARD;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		this.fetchSize = rows;
	}

	@Override
	public int getFetchSize() throws SQLException {
		return fetchSize;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		return 0;
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		batchList.add(QueryCompiler.compile(sql));
	}

	@Override
	public void clearBatch() throws SQLException {
		batchList.clear();
		results.clear();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		int size = batchList.size();
		int[] batchResults = new int[size];

		results = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			try {
				results.add(getConnection().getStorage().query(batchList.get(i)));
				batchResults[i] = 1;
			} catch (RuntimeException any) {
				results.add(null);
				batchResults[i] = 0;
			}
		}

		return batchResults;
	}

	@Override
	public LocalStorageConnection getConnection() throws SQLException {
		return connection;
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		return current < results.size();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return null;
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		return 0;
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		return 0;
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		return 0;
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		return false;
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return false;
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		return false;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return 0;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return isClosed;
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {

	}

	@Override
	public boolean isPoolable() throws SQLException {
		return false;
	}

	@Override
	public void closeOnCompletion() throws SQLException {

	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s=(\n\tqueries=[\n\t\t%s\n\t]\n)", this.getClass().getSimpleName(),
				batchList.stream().map(query -> query.toString()).collect(Collectors.joining("\n\t\t-")));
	}

}
