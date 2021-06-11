/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import adn.service.resource.engine.Storage;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;
import adn.service.resource.engine.tuple.ResourceTuplizer;
import adn.service.resource.engine.tuple.ResourceTuplizerImpl;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTemplateImpl implements ResourceTemplate {

	public static final short DEFAULT_PATH_INDEX = 0;
	public static final short DEFAULT_EXTENSION_INDEX = 1;
	public static final short DEFAULT_CONTENT_INDEX = 2;

	private final String name;

	private final String directoryPath;
	private final Map<String, Integer> indexMap;
	private final String[] columnNames;
	private final Class<?>[] columnTypes;
	private final PropertyAccessImplementor[] accessors;
	private final PojoInstantiator<File> instantiator;

	private final ResourceTuplizer tuplizer;

	private final Storage storage;

	// @formatter:off
	public ResourceTemplateImpl(
			String templateName,
			String directoryPath,
			String[] columnNames,
			Class<?>[] columnTypes,
			PropertyAccessImplementor[] accessors,
			PojoInstantiator<File> instantiator,
			Storage storage) {
	// @formatter:on
		super();
		this.name = templateName;
		this.columnNames = columnNames;
		indexMap = IntStream.range(0, this.columnNames.length)
				.mapToObj(index -> Map.entry(this.columnNames[index], index))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		this.columnTypes = columnTypes;
		this.accessors = accessors;
		this.instantiator = instantiator;
		this.tuplizer = new ResourceTuplizerImpl(this, instantiator);
		this.directoryPath = new StringBuilder(storage.getDirectory())
				.append(directoryPath != null ? directoryPath : "").toString();
		this.storage = storage;
	}

	@Override
	public String getTemplateName() {
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
	public String toString() {
		// @formatter:off
		return String.format("%s: %s\n"
				+ "\t-directory: [%s]\n"
				+ "\t-instantiator: [%s]\n"
				+ "\t-columnNames: [%s]\n"
				+ "\t-columnTypes: [%s]\n"
				+ "\t-accessors: [\n\t\t-%s\n\t]", this.getClass().getSimpleName(), name,
				directoryPath,
				instantiator.toString(),
				Stream.of(columnNames).collect(Collectors.joining(", ")),
				Stream.of(columnTypes).map(type -> type.getName()).collect(Collectors.joining(", ")),
				Stream.of(accessors).map(access -> access.toString()).collect(Collectors.joining("\n\t\t-")));
		// @formatter:on
	}

	@Override
	public ResourceTuplizer getTuplizer() {
		return tuplizer;
	}

	@Override
	public String getDirectory() {
		return directoryPath;
	}

	@Override
	public PropertyAccessImplementor getPropertyAccess(Integer i) {
		return i == null || i < 0 || i > accessors.length ? null : accessors[i];
	}

	@Override
	public PropertyAccessImplementor getPropertyAccess(String columnName) {
		return getPropertyAccess(indexMap.get(columnName));
	}

	@Override
	public Integer getColumnIndex(String columnName) {
		return indexMap.get(columnName);
	}

	@Override
	public String getPathColumn() {
		return columnNames[DEFAULT_PATH_INDEX];
	}

	@Override
	public Class<?> getPathType() {
		return columnTypes[DEFAULT_PATH_INDEX];
	}

	@Override
	public String getExtensionColumn() {
		return columnNames[DEFAULT_EXTENSION_INDEX];
	}

	@Override
	public Class<?> getExtensionType() {
		return columnTypes[DEFAULT_EXTENSION_INDEX];
	}

	@Override
	public String getContentColumn() {
		return columnNames[DEFAULT_CONTENT_INDEX];
	}

	@Override
	public Class<?> getContentType() {
		return columnTypes[DEFAULT_CONTENT_INDEX];
	}

	@Override
	public int getPropertySpan() {
		return columnNames.length;
	}

	@Override
	public Storage getStorage() {
		return storage;
	}

}
