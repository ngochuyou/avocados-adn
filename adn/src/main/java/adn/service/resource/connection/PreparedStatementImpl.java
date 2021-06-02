/**
 * 
 */
package adn.service.resource.connection;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.engine.ResultSetImplementor;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;

/**
 * @author Ngoc Huy
 *
 */
public class PreparedStatementImpl extends StatementImpl implements PreparedStatement {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Query query;

	private int current = -1;
	private List<Query> batches = new ArrayList<>();
	private List<ResultSetImplementor> results = new ArrayList<>();

	public PreparedStatementImpl(LocalStorageConnection connection) {
		super(connection);
	}

	@Override
	protected void clearResults() throws SQLException {
		current = -1;
		results.clear();
	}

	@Override
	public synchronized ResultSet executeQuery() throws SQLException {
		checkClose();
		clearResults();

		for (Query batch : batches) {
			results.add(getConnection().getStorage().query(batch));
		}

		current = 0;

		return getResultSet();
	}

	@Override
	public synchronized int executeUpdate() throws SQLException {
		checkClose();
		clearResults();

		try {
			for (Query batch : batches) {
				if (batch.getType().equals(QueryCompiler.QueryType.FIND)) {
					logger.trace(String.format("Skipping FIND query [%s]", batch.toString()));
					continue;
				}

				results.add(getConnection().getStorage().execute(batch));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return 1;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return results.get(current);
	}

	private Query getQuery() {
		return query;
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		getQuery().setParameterValue(parameterIndex, null);
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void clearParameters() throws SQLException {
		getQuery().clear();
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public boolean execute() throws SQLException {
		return false;
	}

	private Query getBatch() {
		return batches.get(current);
	}

	@Override
	public void addBatch() throws SQLException {
		getBatch().batch(query);
		query = QueryCompiler.compile(query);
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		batches.add(QueryCompiler.compile(sql));
		current++;
		query = getBatch();
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {

	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {

	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {

	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {

	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return null;
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		getQuery().setParameterValue(parameterIndex, x);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		setNull(parameterIndex, sqlType);
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {

	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return null;
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {

	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		getQuery().setParameterValue(parameterIndex, value);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {

	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {

	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {

	}

	@Override
	public synchronized int[] executeBatch() throws SQLException {
		checkClose();

		int batchSize = batches.size();
		int[] updateCounts = new int[batches.size()];

		clearResults();

		for (int i = 0; i < batchSize; i++) {
			try {
				this.results.add(getConnection().getStorage().query(batches.get(i)));
				updateCounts[i] = this.results.get(i).getInt(0);
			} catch (RuntimeException e) {
				e.printStackTrace();
				updateCounts[i] = 0;
			}
		}

		return updateCounts;
	}

	@Override
	public String toString() {
		return String.format("%s=(\n\tbatches=[\n\t\t%s\n\t]\n)", this.getClass().getSimpleName(),
				batches.stream().map(query -> query.toString()).collect(Collectors.joining("\n\t\t-")));
	}

}
