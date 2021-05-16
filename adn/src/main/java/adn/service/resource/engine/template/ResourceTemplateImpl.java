/**
 * 
 */
package adn.service.resource.engine.template;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.property.access.spi.PropertyAccess;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTemplateImpl implements ResourceTemplate {

	private final String name;
	private final Class<?> systemType;

	private final String[] columnNames;
	private final Class<?>[] columnTypes;
	private final PropertyAccess[] accessors;

	public ResourceTemplateImpl(String name, Class<?> systemType, String[] columnNames, Class<?>[] columnTypes,
			PropertyAccess[] accessors) {
		super();
		this.name = name;
		this.systemType = systemType;
		this.columnNames = columnNames;
		this.columnTypes = columnTypes;
		this.accessors = accessors;
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
	public PropertyAccess[] getPropertyAccessors() {
		return accessors;
	}

	@Override
	public Class<?> getSystemType() {
		return systemType;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s: %s\n"
				+ "\t-systemType: %s\n"
				+ "\t-columnNames: [%s]\n"
				+ "\t-columnTypes: [%s]\n"
				+ "\t-accessors: [\n\t\t-%s\n\t]", this.getClass().getSimpleName(), name,
				systemType,
				Stream.of(columnNames).collect(Collectors.joining(", ")),
				Stream.of(columnTypes).map(type -> type.getName()).collect(Collectors.joining(", ")),
				Stream.of(accessors).map(access -> access.toString()).collect(Collectors.joining("\n\t\t-")));
		// @formatter:on
	}

}
