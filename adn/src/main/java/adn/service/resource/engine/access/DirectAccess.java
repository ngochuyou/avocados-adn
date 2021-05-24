/**
 * 
 */
package adn.service.resource.engine.access;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterFieldImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterFieldImpl;

/**
 * Provide direct access to a public field
 * 
 * @author Ngoc Huy
 *
 */
public class DirectAccess extends AbstractPropertyAccess {

	DirectAccess(Class<?> ownerType, String fieldName) throws IllegalArgumentException {
		// @formatter:off
		this(locateGetter(ownerType, fieldName)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Unable to locate [%s] to field [%s] in type [%s]", DirectAccess.class.getSimpleName(), fieldName, ownerType.getName()))),
			locateSetter(ownerType, fieldName)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Unable to locate [%s] to field [%s] in type [%s]", DirectAccess.class.getSimpleName(), fieldName, ownerType.getName()))));
		// @formatter:on
	}

	private DirectAccess(Getter getter, Setter setter) {
		super(getter, setter);
	}

	public static Optional<Getter> locateGetter(Class<?> ownerType, String fieldName) {
		try {
			Field field = ownerType.getDeclaredField(fieldName);

			if (!Modifier.isPublic(field.getModifiers())) {
				throw new SecurityException(
						String.format("Unable to directly access to non-public field [%s] in type [%s]", fieldName,
								ownerType.getName()));
			}

			return Optional.of(new GetterFieldImpl(ownerType, fieldName, field));
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return Optional.ofNullable(null);
		}
	}

	public static Optional<Setter> locateSetter(Class<?> ownerType, String fieldName) {
		try {
			Field field = ownerType.getDeclaredField(fieldName);

			if (!Modifier.isPublic(field.getModifiers())) {
				throw new SecurityException(
						String.format("Unable to directly access to non-public field [%s] in type [%s]", fieldName,
								ownerType.getName()));
			}

			return Optional.of(new SetterFieldImpl(ownerType, fieldName, field));
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.DIRECT_ACCESS_STRATEGY;
	}

}
