/**
 * 
 */
package adn.service.resource.connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.service.resource.engine.ResourceUpdateCount;
import adn.service.resource.engine.ResultSetImplementor;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;

/**
 * @author Ngoc Huy
 *
 */
public class StatementImpl implements Statement {

	private volatile LocalStorageConnection connection;
	private boolean isClosed = false;

	private int maxRowsSize = LocalStorageConnection.Settings.MAX_RESULT_SET_ROWS;
	private int maxFieldSize = LocalStorageConnection.Settings.MAX_FIELD_SIZE;

	protected int updateCount = -1;
	private Query query;
	private ResultSetImplementor result;

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
		return getConnection().getStorage().query(QueryCompiler.compile(sql, this));
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return executeUpdateInternal(sql);
	}

	protected synchronized int executeUpdateInternal(String sql) throws SQLException {
		checkClose();
		checkSQL(sql);

		Query query = QueryCompiler.compile(sql, this);

		Assert.isTrue(query.getType() != QueryCompiler.QueryType.FIND,
				String.format("Unable to execute update from a SELECT query: [%s]", sql));

		try {
			result = getConnection().getStorage().execute(query);
		} catch (Exception any) {
			updateCount = 0;
			throw new SQLException(any);
		} finally {
			updateCount = this.result.getInt(0);
		}

		return updateCount;
	}

	protected void checkSQL(String sql) throws SQLException {
		if (!StringHelper.hasLength(sql)) {
			throw new SQLException("Unable to execute empty statement");
		}
	}

	protected void checkClose() throws SQLException {
		if (connection == null) {
			throw new SQLException("Statement was closed");
		}
	}

	@Override
	public synchronized void close() throws SQLException {
		connection = null;
		isClosed = true;
		query = null;
		result.close();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return maxFieldSize;
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		maxFieldSize = max > LocalStorageConnection.Settings.MAX_FIELD_SIZE
				? LocalStorageConnection.Settings.MAX_FIELD_SIZE
				: max;
	}

	@Override
	public int getMaxRows() throws SQLException {
		return maxRowsSize;
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		maxRowsSize = max > LocalStorageConnection.Settings.MAX_RESULT_SET_ROWS
				? LocalStorageConnection.Settings.MAX_RESULT_SET_ROWS
				: max;
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {}

	@Override
	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {}

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
		executeUpdateInternal(sql);
		return result != null && !(result instanceof ResourceUpdateCount);
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return result;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return updateCount;
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

	}

	@Override
	public int getFetchSize() throws SQLException {
		return 0;
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
	public synchronized void addBatch(String sql) throws SQLException {
		query = QueryCompiler.compile(sql, this);
	}

	@Override
	public synchronized void clearBatch() throws SQLException {
		if (query != null) {
			query.clear();
		}
	}

	protected void clearResults() throws SQLException {
		result.close();
	}

	@Override
	public int[] executeBatch() throws SQLException {
		result = getConnection().getStorage().query(query);

		return new int[] { result.getInt(0) };
	}

	@Override
	public LocalStorageConnection getConnection() throws SQLException {
		return connection;
	}

	@Override
	public synchronized boolean getMoreResults(int current) throws SQLException {
		return false;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return null;
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		return executeUpdateInternal(sql);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		return executeUpdateInternal(sql);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		return executeUpdateInternal(sql);
	}

	private boolean isLastEmptyOrUpdateCount() {
		return result == null || result instanceof ResourceUpdateCount;
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		executeUpdateInternal(sql);

		return !isLastEmptyOrUpdateCount();
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		executeUpdateInternal(sql);

		return !isLastEmptyOrUpdateCount();
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		executeUpdateInternal(sql);

		return !isLastEmptyOrUpdateCount();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return result.getHoldability();
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
		return String.format("%s=(\n\tquery=[\n\t\t%s\n\t]\n)", this.getClass().getSimpleName(), query);
	}

}
