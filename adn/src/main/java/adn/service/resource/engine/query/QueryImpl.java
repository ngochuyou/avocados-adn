/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ngoc Huy
 *
 */
public class QueryImpl implements Query {

	private volatile boolean isLocked = false;

	private Statement statement;
	private String actualSQLString;

	private String templateName;
	private QueryCompiler.QueryType queryType;

	private Map<String, Integer> indexMap = new HashMap<>();
	protected ArrayList<String> columnNames = new ArrayList<>();
	private ArrayList<String> aliasList = new ArrayList<>();
	private ArrayList<Object> values = new ArrayList<>();

	private Query next = null;

	QueryImpl() {}

	/**
	 * For cloning, not batching
	 * 
	 * @param other
	 */
	QueryImpl(Query other) {
		if (other instanceof QueryImpl) {
			QueryImpl sibling = (QueryImpl) other;

			this.indexMap = sibling.indexMap;
			this.columnNames = sibling.columnNames;
			this.aliasList = sibling.aliasList;

			int valuesSize = sibling.values.size();

			this.values = new ArrayList<>(valuesSize);
			IntStream.range(0, valuesSize).forEach(index -> this.values.add(null));
		}

		this.statement = other.getStatement();
		this.actualSQLString = other.getActualSQLString();
		this.templateName = other.getTemplateName();
		this.queryType = other.getType();
	}

	protected void checkLock() throws SQLException {
		if (!isLocked) {
			return;
		}

		throw new SQLException(String.format("[%s] is locked", this.getClass().getSimpleName()));
	}

	@Override
	public QueryCompiler.QueryType getType() {
		return queryType;
	}

	@Override
	public Query setParameterValue(int i, Object value) throws SQLException {
		// Hibernate sets query parameter from 1
		int actualIndex = i - 1;

		values.set(actualIndex, value);
		return this;
	}

	@Override
	public Query setParameterValue(String name, Object param) throws SQLException {
		values.set(indexMap.get(name), param);
		return this;
	}

	@Override
	public synchronized Query lockQuery() {
		this.isLocked = true;
		return this;
	}

	@Override
	public synchronized Query unLockQuery() {
		this.isLocked = false;
		return this;
	}

	QueryImpl addAliasName(String alias, String columnName) throws SQLException {
		checkLock();
		// we assume columnNames.contains(columnName) == true
		// which means, columnName must always be added before adding alias
		aliasList.set(columnNames.indexOf(columnName), alias);
		return this;
	}

	QueryImpl addColumnName(String name) throws SQLException {
		checkLock();
		columnNames.add(name); // add a new column
		aliasList.add(name); // add an alias column with the actual column name
		values.add(null); // add a value placeholder
		indexMap.put(name, columnNames.size() - 1); // map column to index
		return this;
	}

	QueryImpl setQueryType(QueryCompiler.QueryType type) throws SQLException {
		checkLock();
		this.queryType = type;
		return this;
	}

	QueryImpl setTemplateName(String templateName) throws SQLException {
		checkLock();
		this.templateName = templateName;
		return this;
	}

	QueryImpl setActualSQLString(String actualSQLString) throws SQLException {
		checkLock();
		this.actualSQLString = actualSQLString;
		return this;
	}

	QueryImpl setStatement(Statement statement) throws SQLException {
		checkLock();
		this.statement = statement;
		return this;
	}

	@Override
	public String getTemplateName() {
		return templateName;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("\n\t%s %s\n\t(%s)\n\tVALUES (%s)%s", queryType, templateName,
				columnNames.stream()
					.collect(Collectors.joining(", ")),
				values.stream().map(param -> param == null ? null : param.toString())
					.collect(Collectors.joining(", ")), next == null ? "" : "" + next.toString());
		// @formatter:on
	}

	@Override
	public void clear() {
		columnNames.clear();
		indexMap.clear();
	}

	@Override
	public Object getParameterValue(String paramName) {
		return values.get(locateIndex(paramName));
	}

	/**
	 * Try to locate an index via actual column name or column alias
	 */
	private int locateIndex(String key) {
		int aliasIndex = aliasList.indexOf(key); // try alias

		if (aliasIndex != -1) {
			String columnName = columnNames.get(aliasIndex);

			return indexMap.get(columnName);
		}
		// try literal column name
		return indexMap.get(key);
	}

	@Override
	public void batch(Query query) {
		if (query == this) {
			return;
		}

		if (this.next == null) {
			this.next = query;
			return;
		}

		this.next.batch(query);
	}

	@Override
	public Query next() {
		return next;
	}

	@Override
	public String getActualSQLString() {
		return actualSQLString;
	}

	@Override
	public Statement getStatement() {
		return statement;
	}

	@Override
	public String[] getColumnNames() {
		return columnNames.toArray(String[]::new);
	}

	@Override
	public String[] getColumnAlias() {
		return aliasList.toArray(String[]::new);
	}

	@Override
	public String getColumnName(String alias) {
		return columnNames.get(aliasList.indexOf(alias));
	}

	@Override
	public String getAliasName(String columnName) {
		return aliasList.get(columnNames.indexOf(columnName));
	}

}
