/**
 * 
 */
package adn.service.resource.engine.template;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessDelegate;
import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTemplateImpl<T> implements ResourceTemplate<T> {

	private final String name;
	private final Class<T> systemType;

	private final String[] columnNames;
	private final Class<?>[] columnTypes;
	private final PropertyAccessDelegate[] accessors;
	private final ResourceInstantiator<T> instantiator;

	public ResourceTemplateImpl(String name, Class<T> systemType, String[] columnNames, Class<?>[] columnTypes,
			PropertyAccessDelegate[] accessors, ResourceInstantiator<T> instantiator) {
		super();
		this.name = name;
		this.systemType = systemType;
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
	public PropertyAccessDelegate[] getPropertyAccessors() {
		return accessors;
	}

	@Override
	public Class<T> getSystemType() {
		return systemType;
	}

	@Override
	public ResourceInstantiator<T> getInstantiator() {
		return instantiator;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s: %s\n"
				+ "\t-systemType: %s\n"
				+ "\t-instantiator: [%s]\n"
				+ "\t-columnNames: [%s]\n"
				+ "\t-columnTypes: [%s]\n"
				+ "\t-accessors: [\n\t\t-%s\n\t]", this.getClass().getSimpleName(), name,
				systemType,
				instantiator.toString(),
				Stream.of(columnNames).collect(Collectors.joining(", ")),
				Stream.of(columnTypes).map(type -> type.getName()).collect(Collectors.joining(", ")),
				Stream.of(accessors).map(access -> access.toString()).collect(Collectors.joining("\n\t\t-")));
		// @formatter:on
	}

}
