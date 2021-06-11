/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

	private Map<Integer, String> paramNameMap = new HashMap<>();
	private Map<String, Object> paramMap = new LinkedHashMap<>();

	private Query next = null;

	QueryImpl() {}

	QueryImpl(Query parent) {
		if (parent instanceof QueryImpl) {
			QueryImpl sibling = (QueryImpl) parent;

			this.paramNameMap = sibling.paramNameMap;
		}

		this.statement = parent.getStatement();
		this.actualSQLString = parent.getActualSQLString();
		this.templateName = parent.getTemplateName();
		this.queryType = parent.getType();
	}

	private void checkLock() throws SQLException {
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
		int actualIndex = i - 1; // Hibernate sets query params from 1

		paramMap.put(paramNameMap.get(actualIndex), value);

		return this;
	}

	@Override
	public Query setParameterValue(String name, Object param) throws SQLException {
		paramMap.put(name, param);
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

	private int nextIndex = 0;

	QueryImpl addColumnName(String name) throws SQLException {
		checkLock();
		paramMap.put(name, null);
		paramNameMap.put(nextIndex++, name);
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
				paramMap.keySet().stream()
					.collect(Collectors.joining(", ")),
				paramMap.values().stream().map(param -> param == null ? null : param.toString())
					.collect(Collectors.joining(", ")), next == null ? "" : "" + next.toString());
		// @formatter:on
	}

	@Override
	public Query clear() {
		paramMap.clear();
		paramNameMap.clear();
		return this;
	}

	@Override
	public Object getParameterValue(String paramName) {
		return paramMap.get(paramName);
	}

	@Override
	public void batch(Query query) {
		if (query.equals(this)) {
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
	public Collection<String> getParameterNames() {
		return paramNameMap.values();
	}

}
