/**
 * 
 */
package adn.service.resource.storage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import adn.service.resource.local.ManagerFactoryEventListener;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;
import adn.service.resource.storage.ResultSetMetaDataImpl.AccessImpl.DirectAccess;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResultSetMetaDataImpl implements ResultSetMetaDataImplementor, ManagerFactoryEventListener {

	public static final ResultSetMetaDataImpl INSTANCE = new ResultSetMetaDataImpl();

	private static final Logger logger = LoggerFactory.getLogger(ResultSetMetaDataImpl.class);
	private final Map<String, Integer> columnIndexMap = new HashMap<>();
	private List<PropertyAccess> propertyAccessors = null;

	private Access access;

	private ResultSetMetaDataImpl() {
		listen();
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

		private void addColumn(String name, int index) {
			if (columnIndexMap.containsKey(name)) {
				throw new IllegalArgumentException(String.format("Duplicate property [%s]", name));
			}

			columnIndexMap.put(name, index);
		}

		@Override
		public void addColumn(String name) throws NoSuchFieldException {
			Assert.isTrue(StringHelper.hasLength(name), "name must not be empty");

			int nextIndex = getNextColumnIndex();

			logger.trace(String.format("Adding column %s with index %d", name, nextIndex));

			try {
				if (File.class.getDeclaredField(name) != null) {
					addColumn(name, nextIndex);
					addPropertyAccess(new DirectAccess(File.class.getDeclaredField(name)), nextIndex);
				}
			} catch (NoSuchFieldException nsfe) {
				logger.trace(String.format("%s not found, trying to locate getter", name));

				PropertyAccess pa = locatePropertyAccess(name);

				if (pa == null) {
					logger.trace(String.format("Getter not found for [%s]", name));
					throw nsfe;
				}

				addColumn(name, nextIndex);
				addPropertyAccess(pa, nextIndex);
			} catch (SecurityException se) {
				logger.trace(String.format("%s found on [%s], trying to find getter", SecurityException.class, name));

				PropertyAccess pa = locatePropertyAccess(name);

				if (pa == null) {
					logger.trace(String.format("Getter not found for [%s]", name));
					throw new NoSuchFieldException(String.format("Unable to locate field %s in %s", name, File.class));
				}

				addColumn(name, nextIndex);
				addPropertyAccess(pa, nextIndex);
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
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public PropertyAccessStrategy getPropertyAccessStrategy() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Getter getGetter() {
						// TODO Auto-generated method stub
						return getter;
					}
				};
			} catch (NoSuchMethodException | SecurityException pnfe) {
				logger.trace("Failed to locate getter, trying " + LiteralNamedPropertyAccess.class.getName());

				try {
					return new LiteralNamedPropertyAccess(File.class, name);
				} catch (NoSuchMethodException | SecurityException e) {
					logger.trace(String.format("Literal getter not found for [%s]", name));

					return null;
				}
			}
		}

		public class LiteralNamedPropertyAccess implements PropertyAccess {

			private final Getter getter;

			public LiteralNamedPropertyAccess(Class<?> owner, String literalMethodName_aka_FieldName)
					throws NoSuchMethodException, SecurityException {
				// TODO Auto-generated constructor stub
				getter = new GetterMethodImpl(owner, literalMethodName_aka_FieldName,
						owner.getDeclaredMethod(literalMethodName_aka_FieldName));
			}

			@Override
			public PropertyAccessStrategy getPropertyAccessStrategy() {
				// TODO Auto-generated method stub
				return new PropertyAccessStrategy() {

					@Override
					public PropertyAccess buildPropertyAccess(@SuppressWarnings("rawtypes") Class containerJavaType,
							String propertyName) {
						// TODO Auto-generated method stub
						try {
							return new LiteralNamedPropertyAccess(containerJavaType, propertyName);
						} catch (NoSuchMethodException | SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
					}
				};
			}

			@Override
			public Getter getGetter() {
				// TODO Auto-generated method stub
				return getter;
			}

			@Override
			public Setter getSetter() {
				// TODO Auto-generated method stub
				return null;
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
							// TODO Auto-generated catch block
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
						// TODO Auto-generated catch block
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

		private final NonPropertyAccess holder = new NonPropertyAccess();

		@Override
		public void addExplicitlyHydratedColumn(String name) {
			Assert.isTrue(StringHelper.hasLength(name), "name must not be empty");

			int nextIndex = getNextColumnIndex();

			logger.trace(String.format("Adding explicitly hydrated column %s with index %d", name, nextIndex));

			addColumn(name, nextIndex);
			addPropertyAccess(holder, nextIndex);
		}

		public class NonPropertyAccess implements PropertyAccess {

			private NonPropertyAccess() {}

			@Override
			public PropertyAccessStrategy getPropertyAccessStrategy() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Getter getGetter() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Setter getSetter() {
				// TODO Auto-generated method stub
				return null;
			}

		}

		@Override
		public void addSynthesizedColumn(String name) {
			Assert.isTrue(StringHelper.hasLength(name), "name must not be empty");

			int nextIndex = getNextColumnIndex();

			logger.trace(String.format("Adding synthesized column %s with index %d", name, nextIndex));
			addColumn(name, nextIndex);
			addPropertyAccess(holder, nextIndex);
		}

	}

	@Override
	public PropertyAccess getPropertyAccess(String name) throws IllegalArgumentException, SQLException {
		if (!columnIndexMap.containsKey(name)) {
			throw new IllegalArgumentException("Column " + name + " not found");
		}

		return getPropertyAccess(columnIndexMap.get(name));
	}

	@Override
	public PropertyAccess getPropertyAccess(int index) throws IllegalArgumentException, SQLException {
		assertIndex(index);

		return propertyAccessors.get(index);
	}

	@Override
	public void postBuild(ResourceManagerFactory managerFactory) {
		close();
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
				+ "\t-propertyAccesssors: [%s]", this.getClass(),
				columnIndexMap.entrySet().stream()
					.map(entry -> entry.getKey() + "|" + entry.getValue())
					.collect(Collectors.joining(", ")),
				columnIndexMap.entrySet().stream()
					.map(entry -> propertyAccessors.get(entry.getValue()).getClass().toString())
					.collect(Collectors.joining(", ")));
		// @formatter:on
	}

	private void addPropertyAccess(PropertyAccess pa, int index) {
		int cap = getCapacity(index + 1);
		PropertyAccess[] newPAs = new PropertyAccess[cap];

		for (int i = 0; i < cap; i++) {
			if (i == index) {
				newPAs[i] = pa;
				continue;
			}

			if (propertyAccessors == null || i >= propertyAccessors.size()) {
				newPAs[i] = null;
				continue;
			}

			newPAs[i] = propertyAccessors.get(i);
		}

		propertyAccessors = Arrays.asList(newPAs);
	}

	private int getCapacity(int requested) {
		Assert.isTrue(requested >= 0, String.format("Invali index [%d]", requested));

		if (propertyAccessors == null) {
			return requested;
		}

		return Math.max(requested, propertyAccessors.size());
	}

}
