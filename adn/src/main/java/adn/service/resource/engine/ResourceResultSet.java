/**
 * 
 */
package adn.service.resource.engine;

import static adn.helpers.FunctionHelper.doThrow;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.TypeHelper;

public class ResourceResultSet implements ResultSetImplementor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Statement statement;

	private final Index currentIndex;
	private final ResultSetMetaDataImpl metadata;
	private int direction = FETCH_FORWARD;
	private boolean isClosed = false;

	private Object[] lastRead = null;
	private Object[][] rows;

	public ResourceResultSet(Object[][] rows, ResultSetMetaData metadata, Statement statement) {
		this.rows = rows;
		this.metadata = (ResultSetMetaDataImpl) metadata;
		this.statement = statement;
		currentIndex = new Index(0, this);
	}

	private int getRightBound() throws SQLException {
		return getFetchSize() + 1;
	}

	private boolean inBound() throws SQLException {
		return currentIndex.get() > 0 && currentIndex.get() < getRightBound();
	}

	private void assertBound() throws SQLException {
		if (inBound()) {
			return;
		}

		throw new SQLException(
				String.format("Fetch bound exceeded, current cursor position is [%d]", currentIndex.get()));
	}

	private void checkColumnIndex(int requested) throws SQLException {
		if (requested >= lastRead.length) {
			throw new SQLException(String.format("Column [%d] not found on current row", requested));
		}
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		assertBound();
		checkColumnIndex(columnIndex);

		return lastRead[columnIndex];
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
	public boolean next() throws SQLException {
		if (currentIndex.get() < getRightBound()) {
			currentIndex.increaseOne();

			return inBound();
		}

		return false;
	}

	@Override
	public void close() throws SQLException {
		isClosed = true;
	}

	@Override
	public boolean wasNull() throws SQLException {
		return lastRead == null;
	}

	@SuppressWarnings("unchecked")
	private <T> T typeSafeGet(int columnIndex, Class<T> type) throws SQLException {
		assertBound();
		checkColumnIndex(columnIndex);

		Object val = lastRead[columnIndex];

		if (val == null) {
			return null;
		}

		if (type.isAssignableFrom(val.getClass())) {
			return (T) val;
		}

		return tryCast(val, type, String.format("Type mismatch when trying to get value from column {%d} [%s><%s]",
				columnIndex, val.getClass(), type));
	}

	@SuppressWarnings("unchecked")
	private <T> T tryCast(Object val, Class<?> type, String message) throws SQLException {
		Map<Class<?>, Function<Object, Object>> resolver;

		if (TypeHelper.TYPE_CONVERTER.containsKey(type)) {
			if ((resolver = TypeHelper.TYPE_CONVERTER.get(type)).containsKey(val.getClass())) {
				try {
					return (T) resolver.get(val.getClass()).apply(val);
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}

		throw new SQLException(message);
	}

	private <T> T typeSafeGet(String name, Class<T> type) throws SQLException {
		return typeSafeGet(metadata.getIndex(name), type);
	}

	@Override
	public String getString(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, String.class);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, boolean.class);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, byte.class);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, short.class);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return typeSafeGet(columnIndex, int.class);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return typeSafeGet(columnIndex, long.class);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, float.class);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, double.class);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {

		return typeSafeGet(columnIndex, BigDecimal.class);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, byte[].class);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, Date.class);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, Time.class);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return typeSafeGet(columnIndex, Timestamp.class);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		InputStream stream = typeSafeGet(columnIndex, InputStream.class);

		return new InputStreamReader(stream).getEncoding().equals(Charset.forName("ISO-8859-1").toString()) ? stream
				: doThrow(
						new ClassCastException(
								"Type mismatch when trying to get ascii-InputStream from col " + columnIndex),
						ClassCastException.class);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, String.class);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, boolean.class);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, byte.class);
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, short.class);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, int.class);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, long.class);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, float.class);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return typeSafeGet(columnLabel, double.class);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {

		return typeSafeGet(columnLabel, BigDecimal.class);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, byte[].class);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, Date.class);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {

		return typeSafeGet(columnLabel, Time.class);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(metadata.getIndex(columnLabel));
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {

		return getAsciiStream(metadata.getIndex(columnLabel));
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {

	}

	@Override
	public String getCursorName() throws SQLException {
		return "LIST_INDEX";
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return metadata;
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getObject(metadata.getIndex(columnLabel));
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		return metadata.getIndex(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return typeSafeGet(columnIndex, BigDecimal.class);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return typeSafeGet(columnLabel, BigDecimal.class);
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return currentIndex.get() == 0;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return currentIndex.get() == getRightBound();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return currentIndex.get() == 1;
	}

	@Override
	public boolean isLast() throws SQLException {
		return currentIndex.get() == getRightBound() - 1;
	}

	@Override
	public void beforeFirst() throws SQLException {
		currentIndex.set(0);
	}

	@Override
	public void afterLast() throws SQLException {
		currentIndex.set(getRightBound());
	}

	@Override
	public boolean first() throws SQLException {
		if (getFetchSize() == 0) {
			return false;
		}

		currentIndex.set(1);

		return true;
	}

	@Override
	public boolean last() throws SQLException {
		if (getFetchSize() == 0) {
			return false;
		}

		currentIndex.set(getFetchSize() - 1);

		return true;
	}

	@Override
	public int getRow() throws SQLException {
		return currentIndex.get();
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		if (row == 0) {
			beforeFirst();

			return false;
		}

		if (row > 0) {
			currentIndex.set(row);

			return inBound();
		}

		currentIndex.set(getFetchSize() - Math.abs(row));

		return inBound();
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		if (rows == 0) {
			return inBound();
		}

		int newCursor = currentIndex.get() + rows;

		currentIndex.set(newCursor < 0 ? 0 : newCursor > getFetchSize() ? getFetchSize() : newCursor);

		return inBound();
	}

	@Override
	public boolean previous() throws SQLException {
		if (currentIndex.get() > 0) {
			currentIndex.decreaseOne();

			return inBound();
		}

		return false;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		switch (direction) {
			case FETCH_FORWARD:
			case FETCH_REVERSE: {
				this.direction = direction;
				break;
			}
			default: {
				this.direction = FETCH_UNKNOWN;
			}
		}
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return direction;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new SQLException("This ResultSet is read only");
	}

	@Override
	public int getFetchSize() throws SQLException {
		return rows.length;
	}

	@Override
	public int getType() throws SQLException {
		return TYPE_SCROLL_SENSITIVE;
	}

	@Override
	public int getConcurrency() throws SQLException {
		return CONCUR_READ_ONLY;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void insertRow() throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateRow() throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void deleteRow() throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void refreshRow() throws SQLException {
		// TODO: obtain a Connection to LocalStorage, refresh row via that connection
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void moveToCurrentRow() throws SQLException {

	}

	@Override
	public Statement getStatement() throws SQLException {
		return statement;
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {

		return getObject(columnIndex);
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {

		return null;
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {

		return null;
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {

		return null;
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {

		return null;
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {

		return getObject(columnLabel);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {

		return null;
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {

		return null;
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {

		return null;
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {

		return null;
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		Date date = getDate(columnIndex);

		return date != null ? date : new Date(cal.getTimeInMillis());
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {

		return getDate(findColumn(columnLabel));
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {

		return new Time(getDate(columnIndex, cal).getTime());
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {

		return getTime(findColumn(columnLabel));
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {

		return new Timestamp(getDate(columnIndex, cal).getTime());
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {

		return getTimestamp(findColumn(columnLabel));
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {

		return typeSafeGet(columnIndex, URL.class);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {

		return typeSafeGet(findColumn(columnLabel), URL.class);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	public class IndexedRowId implements RowId {

		private final Integer index;

		public IndexedRowId(Integer index) {
			super();
			this.index = index;
		}

		// stolen from
		// https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
		private byte[] toByteArray(int data) {
			byte[] result = new byte[4];

			result[0] = (byte) ((data & 0xFF000000) >> 24);
			result[1] = (byte) ((data & 0x00FF0000) >> 16);
			result[2] = (byte) ((data & 0x0000FF00) >> 8);
			result[3] = (byte) ((data & 0x000000FF) >> 0);

			return result;
		}

		@Override
		public byte[] getBytes() {
			return toByteArray(index);
		}
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return new IndexedRowId(currentIndex.get());
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return new IndexedRowId(currentIndex.get());
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public int getHoldability() throws SQLException {
		// close this when commit
		return CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return isClosed;
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {

		return null;
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {

		return null;
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {

		return null;
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {

		return null;
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return getString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return getString(findColumn(columnLabel));
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException("This ResultSet is readonly");
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return typeSafeGet(columnIndex, type);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return getObject(findColumn(columnLabel), type);
	}

	public boolean readCurrentRow() throws SQLException {
		if (inBound()) {
			lastRead = rows[currentIndex.get() - 1];

			if (logger.isTraceEnabled()) {
				logger.trace(String.format("\n" + "Reading row:\n\t" + "%s",
					lastRead == null ? null : IntStream.range(0, lastRead.length).mapToObj(index -> {
						try {
							return String.format("{%s} -> [%s]", metadata.getColumnName(index),
									lastRead[index] == null ? "NULL" : lastRead[index].toString());
						} catch (SQLException e) {
							return e.getMessage();
						}
					}).collect(Collectors.joining("\n\t"))));
			}

			return true;
		}

		lastRead = null;

		return false;
	}

}

class Index {

	private final ResourceResultSet resultSet;

	private int current;

	Index(int index, ResourceResultSet resultSet) {
		this.resultSet = resultSet;
	}

	synchronized void set(int index) throws SQLException {
		this.current = index;
		resultSet.readCurrentRow();
	}

	synchronized int get() {
		return current;
	}

	synchronized void increaseOne() throws SQLException {
		this.current++;
		resultSet.readCurrentRow();
	}

	synchronized void decreaseOne() throws SQLException {
		this.current--;
		resultSet.readCurrentRow();
	}

}