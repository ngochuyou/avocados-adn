/**
 * 
 */
package adn.service.resource.engine.query;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractQuery implements Query {

	private final QueryType type;
	private final String[] columnNames;
	private final Object[] parameters;

	public AbstractQuery(QueryType type, String[] columnNames, Object[] parameters) {
		this.type = type;
		this.columnNames = columnNames;
		this.parameters = parameters;
	}

	@Override
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public QueryType getType() {
		return type;
	}

	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public String toString() {
		return String.format("%s(columnNames=[%s], parameters=[%s])", type.name(),
				Stream.of(columnNames).collect(Collectors.joining(", ")),
				Stream.of(parameters).map(param -> param.toString()).collect(Collectors.joining(", ")));
	}

}
