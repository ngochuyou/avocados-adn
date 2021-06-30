/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.engine.access.PropertyAccessStrategyFactory;
import adn.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.LocalStorage;
import adn.service.resource.engine.Storage;
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
	private final boolean[] columnNullabilities;
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
			boolean[] columnNullabilities,
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
		this.columnNullabilities = columnNullabilities;

		accessors[DEFAULT_PATH_INDEX] = PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY
				.buildPropertyAccess(FILENAME_GETTER, null);
		accessors[DEFAULT_EXTENSION_INDEX] = PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY
				.buildPropertyAccess(EXTENSION_GETTER, null);
		accessors[DEFAULT_CONTENT_INDEX] = determineContentPropertyAccess(columnTypes[DEFAULT_CONTENT_INDEX]);

		this.accessors = accessors;
		this.instantiator = instantiator;
		this.tuplizer = new ResourceTuplizerImpl(this, instantiator);
		this.directoryPath = new StringBuilder(storage.getDirectory())
				.append(directoryPath != null ? directoryPath : "").toString();
		this.storage = storage;
	}

	private PropertyAccessImplementor determineContentPropertyAccess(Class<?> contentType) {
		if (contentType == byte[].class) {
			return PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY.buildPropertyAccess(BYTE_ARRAY_CONTENT_GETTER,
					FILE_CONTENT_BYTE_ARRAY_SETTER);
		}

		throw new IllegalArgumentException(String.format("Unsupported content type [%s]", contentType));
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
				+ "\t-columnNullabilities: [%s]\n"
				+ "\t-accessors: [\n\t\t-%s\n\t]", this.getClass().getSimpleName(), name,
				directoryPath,
				instantiator.toString(),
				Stream.of(columnNames).collect(Collectors.joining(", ")),
				Stream.of(columnTypes).map(type -> type.getName()).collect(Collectors.joining(", ")),
				IntStream.range(0, columnNullabilities.length)
						.mapToObj(index -> String.valueOf(columnNullabilities[index]))
						.collect(Collectors.joining(", ")),
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

	@Override
	public boolean isColumnNullable(int i) {
		return columnNullabilities[i];
	}

	@Override
	public boolean isColumnNullable(String columnName) {
		return isColumnNullable(indexMap.get(columnName));
	}

	@SuppressWarnings("serial")
	private static final Getter BYTE_ARRAY_CONTENT_GETTER = new Getter() {

		@Override
		public Class<byte[]> getReturnType() {
			return byte[].class;
		}

		@Override
		public String getMethodName() {
			return "<synthetic_byte_array_content_getter>";
		}

		@Override
		public Method getMethod() {
			try {
				return this.getClass().getDeclaredMethod("get", Object.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Member getMember() {
			return getMethod();
		}

		@Override
		public Object getForInsert(Object owner, @SuppressWarnings("rawtypes") Map mergeMap,
				SharedSessionContractImplementor session) {
			return get(owner);
		}

		@Override
		public Object get(Object owner) {
			try {
				Path path = ((File) owner).toPath();

				if (Files.size(path) > LocalStorage.MAX_SIZE_IN_ONE_READ) {
					throw new HibernateException(String.format(
							"File size is too large to read into one byte[], max size in one read is [%s] MB",
							LocalStorage.MAX_SIZE_IN_ONE_READ));
				}

				return Files.readAllBytes(path);
			} catch (Exception e) {
				throw new HibernateException(e);
			}
		}
	};

	@SuppressWarnings("serial")
	private static final Setter FILE_CONTENT_BYTE_ARRAY_SETTER = new Setter() {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		@Override
		public void set(Object target, Object value, SessionFactoryImplementor factory) {
			File file = (File) target;
			byte[] content = (byte[]) value;

			try {
				if (isSame(file, content)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skip writing file since the original content and requested content are the same");
					}

					return;
				}

				if (logger.isTraceEnabled()) {
					logger.trace(
							String.format("Writing file [%s], content length [%s]", file.getPath(), content.length));
				}

				Files.write(Paths.get(file.getPath()), content);

				return;
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}

		private boolean isSame(File file, byte[] content) throws IOException {
			if (!file.exists() || !file.isFile() || content == null) {
				return false;
			}

			BufferedInputStream originalBuffer = new BufferedInputStream(new FileInputStream(file));
			BufferedInputStream requestedBuffer;

			try {
				requestedBuffer = new BufferedInputStream(new ByteArrayInputStream(content));
			} catch (RuntimeException ioe) {
				originalBuffer.close();
				throw ioe;
			}

			try {
				int chunkSize = 8192;
				byte[] originalChunk = new byte[chunkSize];
				byte[] requestedChunk = new byte[chunkSize];
				int offset = 0;

				while (originalBuffer.read(originalChunk, offset, chunkSize) != -1
						&& requestedBuffer.read(requestedChunk, offset, chunkSize) != -1) {
					if (Arrays.compare(originalChunk, requestedChunk) != 0) {
						return false;
					}

					if (originalBuffer.read() == -1 || requestedBuffer.read() == -1) {
						return true;
					}

					chunkSize *= 2;
					originalChunk = new byte[chunkSize];
					requestedChunk = new byte[chunkSize];
				}

				return false;
			} finally {
				originalBuffer.close();
				requestedBuffer.close();
			}
		}

		@Override
		public String getMethodName() {
			return "<synthetic_byte_array_content_setter>(byte[])";
		}

		@Override
		public Method getMethod() {
			try {
				return this.getClass().getDeclaredMethod("dummySetMethod", byte[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				return null;
			}
		}

		@SuppressWarnings("unused")
		private void dummySetMethod(byte[] array) {}
	};

	private static final String FILENAME_GROUPNAME = "pathname";
	private static final String EXTENSION_GROUPNAME = "extension";
	private static final Pattern PATH_PATTERN;

	static {
		String path = "[\\w\\d_-]+(\\\\)?";
		PATH_PATTERN = Pattern.compile(String.format("^(?<dir>(%s)+)?(?<%s>(%s)+)(?<%s>\\.[\\w\\d]+)$",
				Pattern.quote("[\\w\\d]+:\\") + path, FILENAME_GROUPNAME, path, EXTENSION_GROUPNAME));
	}

	@SuppressWarnings("serial")
	private static final Getter FILENAME_GETTER = new Getter() {

		@Override
		public Class<String> getReturnType() {
			return String.class;
		}

		@Override
		public String getMethodName() {
			return "<synthetic_filename_getter>";
		}

		@Override
		public Method getMethod() {
			try {
				return this.getClass().getDeclaredMethod("get", Object.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Member getMember() {
			return getMethod();
		}

		@Override
		public Object getForInsert(Object owner, @SuppressWarnings("rawtypes") Map mergeMap,
				SharedSessionContractImplementor session) {
			return get(owner);
		}

		@Override
		public Object get(Object owner) {
			File file = (File) owner;
			Matcher matcher = PATH_PATTERN.matcher(file.getName());

			matcher.matches();

			return matcher.replaceAll(String.format("${%s}${%s}", FILENAME_GROUPNAME, EXTENSION_GROUPNAME));
		}
	};

	@SuppressWarnings("serial")
	private static final Getter EXTENSION_GETTER = new Getter() {

		@Override
		public Class<String> getReturnType() {
			return String.class;
		}

		@Override
		public String getMethodName() {
			return "<synthetic_extension_getter>";
		}

		@Override
		public Method getMethod() {
			try {
				return this.getClass().getDeclaredMethod("get", Object.class);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Member getMember() {
			return getMethod();
		}

		@Override
		public Object getForInsert(Object owner, @SuppressWarnings("rawtypes") Map mergeMap,
				SharedSessionContractImplementor session) {
			return get(owner);
		}

		@Override
		public Object get(Object owner) {
			return "." + FilenameUtils.getExtension(((File) owner).getName());
		}
	};

}
