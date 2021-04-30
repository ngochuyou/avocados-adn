/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.helpers.FunctionHelper.reject;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.el.PropertyNotFoundException;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.PluralAttribute;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Identifier;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.tuple.CreationTimestampGeneration;
import org.hibernate.tuple.UpdateTimestampGeneration;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.VmValueGeneration;

import adn.helpers.StringHelper;
import adn.service.resource.local.ManagerFactoryEventListener;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.metamodel.MetamodelImpl.IdentifierGenerationHolder;
import adn.service.resource.metamodel.MetamodelImpl.NoValueGeneration;

/**
 * @author Ngoc Huy
 *
 */
public class PropertyBinder implements ManagerFactoryEventListener {

	private final PropertyAccessStrategy fieldAccess = new PropertyAccessStrategyFieldImpl();

	public static PropertyBinder INSTANCE = new PropertyBinder();

	private PropertyBinder() {}

	@Override
	public void postBuild(ResourceManagerFactory managerFactory) {
		// TODO Auto-generated method stub
		logger.trace("Cleaning up INSTANCE of type " + this.getClass().getName());
		PropertyBinder.INSTANCE = null;
	}

	public PropertyAccess createPropertyAccess(Class<?> containerJavaType, String propertyName, Class<?> returnedType)
			throws PropertyNotFoundException {
		try {
			logger.trace("Locating camel-cased getter " + StringHelper.toCamel("get " + propertyName, " ")
					+ " return type " + returnedType);
			logger.trace("Locating camel-cased setter " + StringHelper.toCamel("set " + propertyName, " "));
			containerJavaType.getDeclaredMethod(StringHelper.toCamel("get " + propertyName, " "));
			containerJavaType.getDeclaredMethod(StringHelper.toCamel("set " + propertyName, " "), returnedType);
		} catch (NoSuchMethodException | SecurityException e) {
			logger.trace(
					Stream.of(e.getStackTrace()).map(trace -> trace.toString()).collect(Collectors.joining("\n\t")));
			throw new PropertyNotFoundException("Resource of type " + containerJavaType
					+ " must declare camel-cased getter and setter for property " + propertyName);
		}

		return fieldAccess.buildPropertyAccess(containerJavaType, propertyName);
	}

	public <X> ValueGeneration resolveValueGeneration(ResourceType<X> metamodel, Attribute<?, ?> attribute) {
		Field f = (Field) attribute.getJavaMember();

		if (attribute instanceof Identifier && f.getDeclaredAnnotation(GeneratedValue.class) != null) {
			return IdentifierGenerationHolder.INSTANCE;
		}

		if (attribute instanceof PluralAttribute) {
			return NoValueGeneration.INSTANCE;
		}

		if (f.getDeclaredAnnotation(CreationTimestamp.class) != null) {
			if (!doesSupport(f.getType())) {
				throw new IllegalArgumentException(
						"Unable to generate non-java.util.Date for CreationTimestamp attributes");
			}

			CreationTimestampGeneration generation = new CreationTimestampGeneration();

			generation.initialize(f.getDeclaredAnnotation(CreationTimestamp.class), Date.class);

			return generation;
		}

		if (f.getDeclaredAnnotation(UpdateTimestamp.class) != null) {
			if (!doesSupport(f.getType())) {
				throw new IllegalArgumentException(
						"Unable to generate non-java.util.Date for UpdateTimestamp attributes");
			}

			UpdateTimestampGeneration generation = new UpdateTimestampGeneration();

			generation.initialize(f.getDeclaredAnnotation(UpdateTimestamp.class), Date.class);

			return generation;
		}

		if (f.getDeclaredAnnotation(GeneratedValue.class) != null) {
			GeneratorType gta;

			if ((gta = f.getDeclaredAnnotation(GeneratorType.class)) == null) {
				throw new IllegalArgumentException("GeneratorType required on GeneratedValue attributes");
			}

			VmValueGeneration generation = new VmValueGeneration();

			generation.initialize(gta, f.getType());

			return generation;
		}

		return NoValueGeneration.INSTANCE;
	}

	private boolean doesSupport(Class<?> type) {
		for (Class<?> supported : new Class<?>[] { Date.class, Calendar.class, java.sql.Date.class, Time.class,
				Timestamp.class, Instant.class, LocalDate.class, LocalDateTime.class, LocalTime.class, MonthDay.class,
				OffsetDateTime.class, OffsetTime.class, Year.class, YearMonth.class, ZonedDateTime.class }) {
			if (supported == type) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public <K, V> KeyValueContext<K, V> determineMapGenericType(Field f) {
		if (Map.class.isAssignableFrom(f.getType())) {
			throw new IllegalArgumentException("Unable to extract key, value type out of none-Map collection");
		}

		ParameterizedType paramType = (ParameterizedType) f.getGenericType();

		return new KeyValueContext<>((Class<K>) paramType.getActualTypeArguments()[0],
				(Class<V>) paramType.getActualTypeArguments()[1]);
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> determineNonMapGenericType(Field f) {
		ParameterizedType paramType = (ParameterizedType) f.getGenericType();

		return (Class<T>) paramType.getActualTypeArguments()[0];
	}

	class KeyValueContext<K, V> {

		Class<K> keyType;

		Class<V> valueType;

		public KeyValueContext(Class<K> keyType, Class<V> valueType) {
			super();
			this.keyType = keyType;
			this.valueType = valueType;
		}

	}

	public boolean isOptional(Member f) {
		if (!(f instanceof AccessibleObject)) {
			throw new IllegalArgumentException("Invalid member " + f.getName());
		}

		Column colAnno = ((AccessibleObject) f).getDeclaredAnnotation(Column.class);

		if (colAnno == null) {
			return false;
		}

		return colAnno.nullable();
	}

	public boolean isIdentifierPresented(Class<?> clazz) throws SecurityException, IllegalAccessException {
		// @formatter:off
		long n = 0;
		
		return (n = Stream.of(clazz.getDeclaredFields())
				.map(field -> field.getDeclaredAnnotation(Id.class) != null)
				.filter(pred -> pred)
				.count()) < 2 ? n == 1 : reject("More than one @Id were found in type: " + clazz);
		// @formatter:on
	}

	public boolean isVersionPresented(Class<?> clazz) throws SecurityException, IllegalAccessException {
		// @formatter:off
		long n = 0;
		
		return (n = Stream.of(clazz.getDeclaredFields())
				.map(field -> field.getDeclaredAnnotation(javax.persistence.Version.class) != null)
				.filter(pred -> pred)
				.count()) < 2 ? n == 1 : reject("More than one @Version were found in type: " + clazz);
		// @formatter:on
	}

	public PersistentAttributeType determineNonBasicType(Class<?> type) {
		return Optional
				.ofNullable(Collection.class.isAssignableFrom(type) ? PersistentAttributeType.ELEMENT_COLLECTION
						: PersistentAttributeType.ONE_TO_ONE)
				.orElseThrow(() -> new IllegalArgumentException("Unable to determine type of " + type.getName()));
	}

	public boolean isUpdatable(Field f) {
		Column colAnno = f.getDeclaredAnnotation(Column.class);

		if (colAnno == null) {
			return false;
		}

		return colAnno.updatable();
	}

	enum AttributeRole {

		IDENTIFIER, VERSION, PROPERTY;

		static AttributeRole getRole(Field f) {
			Id idAnno = f.getDeclaredAnnotation(Id.class);
			javax.persistence.Version versionAnno = f.getDeclaredAnnotation(javax.persistence.Version.class);

			if (idAnno != null && versionAnno != null) {
				throw new IllegalArgumentException(
						"@Id and @Version collision on " + f.getDeclaringClass() + "." + f.getName());
			}

			return idAnno != null ? IDENTIFIER : versionAnno != null ? VERSION : PROPERTY;
		}

	}

}
