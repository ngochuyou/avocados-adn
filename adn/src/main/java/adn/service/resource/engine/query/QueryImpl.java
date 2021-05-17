/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

/**
 * @author Ngoc Huy
 *
 */
public class QueryImpl implements Query {

	private volatile boolean isLocked = false;

	private String templateName;
	private QueryCompiler.QueryType queryType;
	private Set<String> columnNames = new LinkedHashSet<>();
	private Object[] parameters = new Object[0];

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
	public Iterator<String> getColumnNames() {
		return columnNames.iterator();
	}

	@Override
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public Query addParameter(int index, Object param) throws SQLException {
		int actualIndex = index - 1;
		LoggerFactory.getLogger(this.getClass()).trace(String.format("Adding parameter [%s]:[%s] -> [%s]", index,
				columnNames.toArray(String[]::new)[actualIndex], param == null ? "NULL" : param.toString()));

		parameters[actualIndex] = param;
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
		columnNames.add(name);
		parameters = new Object[columnNames.size()];
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
		return String.format("%s %s (%s)", queryType, templateName,
				Stream.of(columnNames.toArray(String[]::new)).collect(Collectors.joining(", ")));
	}

	@Override
	public Query clear() {
		Arrays.fill(parameters, null);
		return this;
	}

}
