/**
 * 
 */
package adn.service.resource.engine.access;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;

/**
 * Access of this type can be used in scenarios where either getter or setter
 * method is named exactly like the field name.
 * </p>
 * In such cases, if a getter was given, then the setter must be a non-access
 * 
 * @author Ngoc Huy
 *
 */
public class LiterallyNamedAccess extends AbstractDisabledPropertyAccess {

	LiterallyNamedAccess(Class<?> ownerType, String fieldName) {
		super(locateGetter(ownerType, fieldName).orElse(null), locateSetter(ownerType, fieldName).orElse(null));
	}

	public static Optional<Getter> locateGetter(Class<?> ownerType, String fieldName) {
		try {
			@SuppressWarnings("unused") // for field check only
			Field field = ownerType.getDeclaredField(fieldName);
			Method getterMethod = ownerType.getDeclaredMethod(fieldName);

			return Optional.of(new GetterMethodImpl(ownerType, fieldName, getterMethod));
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			return Optional.ofNullable(null);
		}
	}

	public static Optional<Setter> locateSetter(Class<?> ownerType, String fieldName) {
		try {
			Field field = ownerType.getDeclaredField(fieldName);
			Method setterMethod = bestGuessSetter(ownerType, field, fieldName);

			return Optional.of(new SetterMethodImpl(ownerType, fieldName, setterMethod));
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.LITERALLY_NAMED_ACCESS_STRATEGY;
	}

}
