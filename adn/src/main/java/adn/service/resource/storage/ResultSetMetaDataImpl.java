/**
 * 
 */
package adn.service.resource.storage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;
import adn.service.resource.storage.ResultSetMetaDataImpl.AccessImpl.DirectAccess;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResultSetMetaDataImpl implements ResultSetMetaDataImplementor {

	public static final ResultSetMetaDataImpl INSTANCE = new ResultSetMetaDataImpl();

	private static final Logger logger = LoggerFactory.getLogger(ResultSetMetaDataImpl.class);
	private Map<String, Integer> columnIndexMap = new HashMap<>();
	private List<PropertyAccess> propertyAccessors = new ArrayList<>();

	private Access access;

	private ResultSetMetaDataImpl() {
		access = createAccess();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (isWrapperFor(iface)) {
			return (T) this;
		}

		throw new ClassCastException("Unable to unwrap to " + iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isAssignableFrom(this.getClass());
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columnIndexMap.size();
	}

	private void assertIndex(int index) throws IllegalArgumentException, SQLException {
		Assert.isTrue(index >= 0 && index < getColumnCount(), "Index " + index + " is out of bound");
	}

	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		assertIndex(column);
		return false;
	}

	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		assertIndex(column);
		return true;
	}

	@Override
	public boolean isSearchable(int column) throws SQLException {
		assertIndex(column);
		return true;
	}

	private Class<?> getColumnJavaType(int column) throws IllegalArgumentException, SQLException {
		PropertyAccess access = getPropertyAccess(column);

		return access instanceof DirectAccess ? ((Field) access.getGetter().getMember()).getType()
				: access.getGetter().getReturnType();
	}

	@Override
	public boolean isCurrency(int column) throws SQLException {
		return Currency.class.isAssignableFrom(getColumnJavaType(column));
	}

	@Override
	public int isNullable(int column) throws SQLException {
		assertIndex(column);
		return 0;
	}

	@Override
	public boolean isSigned(int column) throws SQLException {
		assertIndex(column);
		return true;
	}

	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		assertIndex(column);
		return Integer.MAX_VALUE;
	}

	@Override
	public String getColumnLabel(int column) throws SQLException {
		return getColumnName(column);
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		assertIndex(column);

		return columnIndexMap.entrySet().stream().filter(entry -> entry.getValue() == column)
				.map(entry -> entry.getKey()).findFirst().orElse(null);
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		return File.class.getName();
	}

	@Override
	public int getPrecision(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getScale(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		return File.class.getName();
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		return File.class.getName();
	}

	@Override
	public int getColumnType(int column) throws SQLException {
		return 0;
	}

	@Override
	public String getColumnTypeName(int column) throws SQLException {
		return getColumnJavaType(column).getName();
	}

	@Override
	public boolean isReadOnly(int column) throws SQLException {
		return true;
	}

	@Override
	public boolean isWritable(int column) throws SQLException {
		return !isReadOnly(column);
	}

	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return !isReadOnly(column);
	}

	@Override
	public String getColumnClassName(int column) throws SQLException {
		return getColumnName(column);
	}

	private Access createAccess() {
		return new AccessImpl();
	}

	@Override
	public void close() {
		columnIndexMap = columnIndexMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		LoggerFactory.getLogger(this.getClass()).trace("\n" + toString());
		LoggerFactory.getLogger(this.getClass()).trace("Closing access to " + this.getClass().getName());
		this.access = null;
	}

	@Override
	public Access getAccess() throws IllegalAccessException {
		if (access == null) {
			throw new IllegalAccessException("Access to " + ResultSetMetaDataImplementor.class + " was closed");
		}

		return access;
	}

	protected int getNextColumnIndex() {
		return Integer.valueOf(columnIndexMap.size());
	}

	public class AccessImpl implements Access {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private boolean putColumn(String name, int index) {
			if (columnIndexMap.containsKey(name)) {
				logger.trace(String.format("Ignoring duplicated property [%s]", name));
				return false;
			}

			columnIndexMap.put(name, index);
			return true;
		}

		private void registerColumn(String name, int index, PropertyAccess access) {
			if (putColumn(name, index)) {
				propertyAccessors.add(access);
				logger.trace(String.format("Registered new column [name, index, access] -> [%s, %s, %s]", name, index,
						access.toString()));
			}
		}

		@Override
		public void addColumn(String name) throws NoSuchFieldException {
			Assert.isTrue(StringHelper.hasLength(name), "name must not be empty");

			int nextIndex = getNextColumnIndex();

			logger.trace(String.format("Trying to add column [%s]", name));

			try {
				Field field;

				if ((field = File.class.getDeclaredField(name)) != null) {
					if (Modifier.isPublic(field.getModifiers())) {
						registerColumn(name, nextIndex, new DirectAccess(File.class.getDeclaredField(name)));
					}
				}
			} catch (NoSuchFieldException nsfe) {
				logger.trace(String.format("[%s] not found, trying to locate getter", name));

				PropertyAccess pa = locatePropertyAccess(name);

				if (pa == null) {
					logger.trace(String.format("Getter not found for [%s]", name));
					throw nsfe;
				}

				registerColumn(name, nextIndex, pa);
			} catch (SecurityException se) {
				logger.trace(String.format("[%s] found on [%s], trying to find getter", SecurityException.class, name));

				PropertyAccess pa = locatePropertyAccess(name);

				if (pa == null) {
					logger.trace(String.format("Getter not found for [%s]", name));
					throw new NoSuchFieldException(
							String.format("Unable to locate field [%s] in [%s]", name, File.class));
				}

				registerColumn(name, nextIndex, pa);
			}
		}

		private PropertyAccess locatePropertyAccess(String name) {
			try {
				String methodName = StringHelper.toCamel("get " + name, " ");
				Method method = File.class.getDeclaredMethod(methodName);

				return new PropertyAccess() {

					private final Getter getter = new GetterMethodImpl(File.class, methodName, method);

					@Override
					public Setter getSetter() {

						return null;
					}

					@Override
					public PropertyAccessStrategy getPropertyAccessStrategy() {

						return null;
					}

					@Override
					public Getter getGetter() {

						return getter;
					}

					@Override
					public String toString() {
						return String.format("<%s:%s>", GetterMethodImpl.class.getSimpleName(),
								getGetter().getMember().getName());
					}

				};
			} catch (NoSuchMethodException | SecurityException pnfe) {
				logger.trace("Failed to locate getter, trying " + LiterallyNamedPropertyAccess.class.getName());

				try {
					return new LiterallyNamedPropertyAccess(File.class, name);
				} catch (NoSuchMethodException | SecurityException e) {
					logger.trace(String.format("Literal getter not found for [%s]", name));

					return null;
				}
			}
		}

		public class LiterallyNamedPropertyAccess implements PropertyAccess {

			private final Getter getter;

			public LiterallyNamedPropertyAccess(Class<?> owner, String literalMethodName_aka_FieldName)
					throws NoSuchMethodException, SecurityException {
				getter = new GetterMethodImpl(owner, literalMethodName_aka_FieldName,
						owner.getDeclaredMethod(literalMethodName_aka_FieldName));
			}

			@Override
			public PropertyAccessStrategy getPropertyAccessStrategy() {

				return new PropertyAccessStrategy() {

					@Override
					public PropertyAccess buildPropertyAccess(@SuppressWarnings("rawtypes") Class containerJavaType,
							String propertyName) {

						try {
							return new LiterallyNamedPropertyAccess(containerJavaType, propertyName);
						} catch (NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
							return null;
						}
					}
				};
			}

			@Override
			public Getter getGetter() {
				return getter;
			}

			@Override
			public Setter getSetter() {
				return null;
			}

			@Override
			public String toString() {
				return String.format("<%s:%s>", LiterallyNamedPropertyAccess.class.getSimpleName(),
						getGetter().getMember().getName());
			}

		}

		public class DirectAccess implements PropertyAccess {

			private final DirectGetter getter;

			public DirectAccess(Field field) {
				Assert.notNull(field, "Unable to directly access NULL field");
				this.getter = new DirectGetter(field);
			}

			@Override
			public PropertyAccessStrategy getPropertyAccessStrategy() {

				return new PropertyAccessStrategy() {
					@Override
					public PropertyAccess buildPropertyAccess(@SuppressWarnings("rawtypes") Class containerJavaType,
							String propertyName) {

						try {
							return new DirectAccess(containerJavaType.getDeclaredField(propertyName));
						} catch (NoSuchFieldException | SecurityException e) {
							e.printStackTrace();
							return null;
						}
					}
				};
			}

			@Override
			public Getter getGetter() {
				return getter;
			}

			@Override
			public Setter getSetter() {
				return null;
			}

			@Override
			public String toString() {
				return String.format("<%s:%s>", DirectAccess.class.getSimpleName(), getGetter().getMember().getName());
			}

			public class DirectGetter implements Getter {

				private final Field field;

				public DirectGetter(Field field) {
					Assert.notNull(field,
							String.format("Unable to build %s for NULL field", this.getClass().getName()));
					this.field = field;
				}

				@Override
				public Object get(Object owner) {
					try {
						return field.get(owner);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
						return null;
					}
				}

				@Override
				public Object getForInsert(Object owner, @SuppressWarnings("rawtypes") Map mergeMap,
						SharedSessionContractImplementor session) {
					return null;
				}

				@Override
				public Class<?> getReturnType() {
					return field.getType();
				}

				@Override
				public Member getMember() {
					return field;
				}

				@Override
				public String getMethodName() {
					return null;
				}

				@Override
				public Method getMethod() {
					return null;
				}

			}

		}

		private final PropertyAccessHolder holder = new PropertyAccessHolder();

		@Override
		public void addExplicitlyHydratedColumn(String name) {
			Assert.isTrue(StringHelper.hasLength(name), "name must not be empty");

			int nextIndex = getNextColumnIndex();

			logger.trace(String.format("Trying to add explicitly hydrated column [%s]", name));
			registerColumn(name, nextIndex, holder);
		}

		public class PropertyAccessHolder implements PropertyAccess {

			private PropertyAccessHolder() {}

			@Override
			public PropertyAccessStrategy getPropertyAccessStrategy() {
				return null;
			}

			@Override
			public Getter getGetter() {
				return null;
			}

			@Override
			public Setter getSetter() {
				return null;
			}

			@Override
			public String toString() {
				return String.format("<%s>", PropertyAccessHolder.class.getSimpleName());
			}

		}

		@Override
		public void addSynthesizedColumn(String name) {
			Assert.isTrue(StringHelper.hasLength(name), "name must not be empty");

			int nextIndex = getNextColumnIndex();

			logger.trace(String.format("Trying to add synthesized column [%s]", name));
			registerColumn(name, nextIndex, holder);
		}

	}

	@Override
	public PropertyAccess getPropertyAccess(String name) throws IllegalArgumentException, SQLException {
		if (!columnIndexMap.containsKey(name)) {
			throw new IllegalArgumentException(String.format("Column [%s] not found", name));
		}

		return getPropertyAccess(columnIndexMap.get(name));
	}

	@Override
	public PropertyAccess getPropertyAccess(int index) throws IllegalArgumentException, SQLException {
		assertIndex(index);

		return propertyAccessors.get(index);
	}

	@Override
	public int getIndex(String name) {
		if (!columnIndexMap.containsKey(name)) {
			logger.trace(String.format("Unable to get index of column [%s]", name));
			return -1;
		}

		return columnIndexMap.get(name);
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s built with summary: \n"
				+ "\t-columnIndexMap: [%s]\n"
				+ "\t-propertyAccesssors: \n\t\t%s", this.getClass(),
				columnIndexMap.entrySet().stream()
					.map(entry -> entry.getKey() + "|" + entry.getValue())
					.collect(Collectors.joining(", ")),
				columnIndexMap.entrySet().stream()
					.map(entry -> entry.getValue() + " -> " + propertyAccessors.get(entry.getValue()).toString())
					.collect(Collectors.joining("\n\t\t")));
		// @formatter:on
	}

}
