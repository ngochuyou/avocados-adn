/**
 * 
 */
package adn.engine.access;

import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class MethodAccess extends AbstractPropertyAccess {

	MethodAccess(Class<?> owningType, String fieldName, Class<?> fieldType) {
		this(locateGetter(owningType, fieldName).orElseThrow(() -> new IllegalArgumentException(String
				.format("Unable to locate getter for field name [%s] in type [%s]", fieldName, owningType.getName()))),
				locateSetter(owningType, fieldName, fieldType).orElseThrow(() -> new IllegalArgumentException(String
						.format("Unable to locate setter for field name [%s] in type [%s] with argument types [%s]",
								fieldName, owningType.getName()))));
	}

	private MethodAccess(Getter getter, Setter setter) {
		super(getter, setter);
	}

	public static Optional<Getter> locateGetter(Class<?> owningType, String fieldName) {
		try {
			return Optional.of(new GetterMethodImpl(owningType, fieldName, owningType.getMethod(
					StringHelper.toCamel(String.format("%s %s", "get", fieldName), StringHelper.WHITESPACE_CHARS))));
		} catch (NoSuchMethodException | SecurityException any1) {
			try {
				return Optional.of(new GetterMethodImpl(owningType, fieldName, owningType.getMethod(
						StringHelper.toCamel(String.format("%s %s", "is", fieldName), StringHelper.WHITESPACE_CHARS))));
			} catch (NoSuchMethodException | SecurityException any) {
				return Optional.ofNullable(null);
			}
		}
	}

	public static Optional<Setter> locateSetter(Class<?> owningType, String fieldName, Class<?> fieldType) {
		try {
			return Optional.of(new SetterMethodImpl(owningType, fieldName, owningType.getMethod(
					StringHelper.toCamel(String.format("%s %s", "set", fieldName), StringHelper.WHITESPACE_CHARS),
					fieldType)));
		} catch (NoSuchMethodException | SecurityException any) {
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		// TODO Auto-generated method stub
		return null;
	}

}
