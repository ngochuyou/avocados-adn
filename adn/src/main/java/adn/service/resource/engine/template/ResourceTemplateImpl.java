/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

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
	private final ResourceInstantiator<File> instantiator;

	public ResourceTemplateImpl(String name, String pathColumnName, String[] columnNames, Class<?>[] columnTypes,
			PropertyAccessImplementor[] accessors, ResourceInstantiator<File> instantiator) {
		super();
		this.name = name;
		this.pathColumnName = pathColumnName;
		this.columnNames = columnNames;
		this.columnTypes = columnTypes;
		this.accessors = accessors;
		this.instantiator = instantiator;
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
	public ResourceInstantiator<File> getInstantiator() {
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

}
