/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
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

	private String templateName;
	private QueryCompiler.QueryType queryType;

	private Map<Integer, String> paramNameMap = new HashMap<>();
	private Map<String, Object> paramMap = new LinkedHashMap<>();

	QueryImpl() {}

	private void checkQueryLock() throws SQLException {
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

	QueryImpl addColumnName(String name) throws SQLException {
		checkQueryLock();
		paramMap.put(name, null);
		paramNameMap.put(paramMap.size() - 1, name);
		return this;
	}

	QueryImpl setQueryType(QueryCompiler.QueryType type) throws SQLException {
		checkQueryLock();
		this.queryType = type;
		return this;
	}

	QueryImpl setTemplateName(String templateName) throws SQLException {
		checkQueryLock();
		this.templateName = templateName;
		return this;
	}

	@Override
	public String getTemplateName() {
		return templateName;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s %s (%s) VALUES (%s)", queryType, templateName,
				paramMap.keySet().stream()
					.collect(Collectors.joining(", ")),
				paramMap.values().stream().map(param -> param == null ? null : param.toString())
					.collect(Collectors.joining(", ")));
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

}
