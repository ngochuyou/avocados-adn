/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.File;
import java.sql.ResultSetMetaData;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.tuple.Tuplizer;

import adn.service.resource.engine.ResourceTuplizer;
import adn.service.resource.engine.ResultSetMetaDataImpl;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTemplateImpl implements ResourceTemplate {

	private final String name;

	private final String pathColumnName;
	private final String[] columnNames;
	private final Class<?>[] columnTypes;
	private final PropertyAccessImplementor[] accessors;
	private final PojoInstantiator<File> instantiator;

	private final Tuplizer tuplizer;
	private final ResultSetMetaData metadata;

	public ResourceTemplateImpl(String name, String pathColumnName, String[] columnNames, Class<?>[] columnTypes,
			PropertyAccessImplementor[] accessors, PojoInstantiator<File> instantiator) {
		super();
		this.name = name;
		this.pathColumnName = pathColumnName;
		this.columnNames = columnNames;
		this.columnTypes = columnTypes;
		this.accessors = accessors;
		this.instantiator = instantiator;
		this.tuplizer = new ResourceTuplizer(this);
		this.metadata = new ResultSetMetaDataImpl(getName(), getColumnNames());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public Class<?>[] getColumnTypes() {
		return columnTypes;
	}

	@Override
	public PropertyAccessImplementor[] getPropertyAccessors() {
		return accessors;
	}

	@Override
	public PojoInstantiator<File> getInstantiator() {
		return instantiator;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s: %s\n"
				+ "\t-instantiator: [%s]\n"
				+ "\t-columnNames: [%s]\n"
				+ "\t-columnTypes: [%s]\n"
				+ "\t-accessors: [\n\t\t-%s\n\t]", this.getClass().getSimpleName(), name,
				instantiator.toString(),
				Stream.of(columnNames).collect(Collectors.joining(", ")),
				Stream.of(columnTypes).map(type -> type.getName()).collect(Collectors.joining(", ")),
				Stream.of(accessors).map(access -> access.toString()).collect(Collectors.joining("\n\t\t-")));
		// @formatter:on
	}

	@Override
	public String getPathColumnName() {
		return pathColumnName;
	}

	@Override
	public Tuplizer getTuplizer() {
		return tuplizer;
	}

	@Override
	public ResultSetMetaData getResultSetMetaData() {
		return metadata;
	}

}
