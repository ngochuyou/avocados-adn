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

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class StandardAccess extends AbstractPropertyAccess {

	StandardAccess(Class<?> owner, String fieldName) throws IllegalArgumentException {
		try {
			getter = locateGetter(owner, fieldName, true).orElseThrow(() -> new NoSuchMethodException(
					String.format("Unable to locate getter in [%s%s]", owner.getName(), fieldName)));
			setter = locateSetter(owner, fieldName, true).orElseThrow(() -> new NoSuchMethodException(
					String.format("Unable to locate setter in [%s%s]", owner.getName(), fieldName)));
		} catch (SecurityException | NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings("unused")
	static Optional<Getter> locateGetter(Class<?> owner, String fieldName, boolean isFieldRequired) {
		try {
			if (isFieldRequired) {
				Field field = owner.getDeclaredField(fieldName);
			}

			String getterName = StringHelper.toCamel(String.format("%s %s", "get", fieldName),
					StringHelper.ONE_OF_WHITESPACE_CHARS);
			Method getter = owner.getDeclaredMethod(getterName);

			return Optional.of(new GetterMethodImpl(owner, getterName, getter));
		} catch (SecurityException | NoSuchMethodException | NoSuchFieldException e) {
			return Optional.ofNullable(null);
		}
	}

	static Optional<Setter> locateSetter(Class<?> owner, String fieldName, boolean isFieldRequired,
			Class<?>... paramTypes) {
		try {
			String setterName = StringHelper.toCamel(String.format("%s %s", "set", fieldName),
					StringHelper.ONE_OF_WHITESPACE_CHARS);

			if (!isFieldRequired) {
				Method setter = null;

				try {
					setter = owner.getDeclaredMethod(setterName, Object.class);
				} catch (NoSuchMethodException nsme) {
					// since there is no field needed and setter parameter could be primitive type
					// as well, we'll now try to locate it with alternative parameter types
					setter = paramTypes.length == 0 ? null
							: tryToLocateSetterWithAlternativeParamTypes(owner, paramTypes[0], setterName);
				}

				if (setter == null) {
					throw new NoSuchMethodException(
							String.format("[%s] not found in [%s]", setterName, owner.getName()));
				}

				return Optional.of(new SetterMethodImpl(owner, setterName, setter));
			}

			Field field = owner.getDeclaredField(fieldName);
			Method setter = owner.getDeclaredMethod(setterName, field.getType());

			return Optional.of(new SetterMethodImpl(owner, setterName, setter));
		} catch (SecurityException | NoSuchMethodException | NoSuchFieldException e) {

			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.STANDARD_ACCESS_STRATEGY;
	}

}
