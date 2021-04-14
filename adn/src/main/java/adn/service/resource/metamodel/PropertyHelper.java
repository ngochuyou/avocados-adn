/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
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
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.metamodel.Attribute;

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

import adn.service.resource.metamodel.MetamodelImpl.IdentifierGenerationHolder;
import adn.service.resource.metamodel.MetamodelImpl.NoValueGeneration;

/**
 * @author Ngoc Huy
 *
 */
public class PropertyHelper {

	private PropertyAccessStrategy fieldAccess = new PropertyAccessStrategyFieldImpl();
	
	public PropertyAccess createPropertyAccess(Class<?> containerJavaType, String propertyName) {
		
		return fieldAccess.buildPropertyAccess(containerJavaType, propertyName);
	}
	
	public <X> ValueGeneration resolveValueGeneration(ResourceType<X> metamodel, Attribute<?, ?> attribute) {
		Field f = (Field) attribute.getJavaMember();

		if (attribute instanceof Identifier && f.getDeclaredAnnotation(GeneratedValue.class) != null) {
			return IdentifierGenerationHolder.INSTANCE;
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

}
