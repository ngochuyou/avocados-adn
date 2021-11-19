/**
 * 
 */
package adn.engine.access;

import java.lang.reflect.Method;
import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;

import adn.helpers.StringHelper;

/**
 * Access of this type could be used in any cases, field presence is not
 * required
 * 
 * @author Ngoc Huy
 *
 */
public class PropertyAccessDelegate extends AbstractPropertyAccess {

	PropertyAccessDelegate(Class<?> ownerType, String propertyName, boolean isFieldRequired,
			Class<?>... parameterTypes) {
		this(locateGetter(ownerType, propertyName, isFieldRequired).orElse(null),
				locateSetter(ownerType, propertyName, isFieldRequired, parameterTypes).orElse(null));
	}

	private PropertyAccessDelegate(Getter getter, Setter setter) {
		super(getter, setter);
	}

	public static Optional<Getter> locateGetter(Class<?> ownerType, String propertyName, boolean isFieldRequired) {
		if (isFieldRequired) {
			// @formatter:off
			return Optional.of(StandardAccess.locateGetter(ownerType, propertyName)
					.orElse(LiterallyNamedAccess.locateGetter(ownerType, propertyName)
							.orElse(DirectAccess.locateGetter(ownerType, propertyName)
									.orElse(null))));
			// @formatter:on
		}
		// check field presence
		Method getterMethod;
		boolean isFieldPresents = true;

		try {
			ownerType.getDeclaredField(propertyName);
		} catch (NoSuchFieldException e) {
			isFieldPresents = false;
		}

		if (isFieldPresents) {
			return locateGetter(ownerType, propertyName, true);
		}
		// try getXXX() or isXXX();
		try {
			getterMethod = bestGuessGetter(ownerType, propertyName);

			return Optional.of(new GetterMethodImpl(ownerType, propertyName, getterMethod));
		} catch (NoSuchMethodException nsme) {}
		// try literally named method
		try {
			getterMethod = ownerType.getDeclaredMethod(propertyName); // a.k.a method name

			return Optional.of(new GetterMethodImpl(ownerType, propertyName, getterMethod));
		} catch (NoSuchMethodException e) {}

		return Optional.ofNullable(null);
	}

	public static Optional<Setter> locateSetter(Class<?> ownerType, String propertyName, boolean isFieldRequired,
			Class<?>... parameterTypes) {
		if (isFieldRequired) {
			// @formatter:off
			return Optional.of(StandardAccess.locateSetter(ownerType, propertyName)
					.orElse(LiterallyNamedAccess.locateSetter(ownerType, propertyName)
							.orElse(DirectAccess.locateSetter(ownerType, propertyName)
									.orElse(null))));
			// @formatter:on
		}

		Method setterMethod;
		boolean isFieldPresents = true;

		try {
			ownerType.getDeclaredField(propertyName);
		} catch (NoSuchFieldException e) {
			isFieldPresents = false;
		}

		if (isFieldPresents) {
			return locateSetter(ownerType, propertyName, true, parameterTypes);
		}

		Class<?> parameterType = parameterTypes.length == 0 ? Object.class : parameterTypes[0];
		// try setXXX();
		try {
			String setterMethodName = StringHelper.toCamel(String.format("%s %s", "set", propertyName),
					StringHelper.WHITESPACE_CHAR_CLASS);

			setterMethod = bestGuessSetter(ownerType, null, setterMethodName, parameterType);

			return Optional.of(new SetterMethodImpl(ownerType, setterMethodName, setterMethod));
		} catch (NoSuchMethodException e) {}
		// try literally named method
		try {
			setterMethod = bestGuessSetter(ownerType, null, propertyName, parameterTypes);

			return Optional.of(new SetterMethodImpl(ownerType, propertyName, setterMethod));
		} catch (NoSuchMethodException e) {}

		return Optional.ofNullable(null);
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.DELEGATED_ACCESS_STRATEGY;
	}

}
