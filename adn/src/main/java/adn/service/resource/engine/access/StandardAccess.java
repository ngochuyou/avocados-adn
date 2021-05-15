/**
 * 
 */
package adn.service.resource.engine.access;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class StandardAccess implements PropertyAccess {

	private final Getter getter;
	private final Setter setter;

	StandardAccess(Class<?> owner, String fieldName) throws IllegalArgumentException {
		try {
			this.getter = locateGetter(owner, fieldName).orElseThrow(() -> new NoSuchMethodException(
					String.format("Unable to locate getter in [%s%s]", owner.getName(), fieldName)));
			this.setter = locateSetter(owner, fieldName).orElseThrow(() -> new NoSuchMethodException(
					String.format("Unable to locate setter in [%s%s]", owner.getName(), fieldName)));
		} catch (SecurityException | NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		}
	}

	static Optional<Getter> locateGetter(Class<?> owner, String fieldName) {
		try {
			@SuppressWarnings("unused") // for exception catching only
			Field field = owner.getDeclaredField(fieldName);
			String getterName = StringHelper.toCamel(String.format("%s %s", "get", fieldName),
					StringHelper.ONE_OF_WHITESPACE_CHARS);
			Method getter = owner.getDeclaredMethod(getterName);

			return Optional.of(new GetterMethodImpl(owner, getterName, getter));
		} catch (SecurityException | NoSuchMethodException | NoSuchFieldException e) {
			return Optional.ofNullable(null);
		}
	}

	static Optional<Setter> locateSetter(Class<?> owner, String fieldName) {
		try {
			Field field = owner.getDeclaredField(fieldName);
			String setterName = StringHelper.toCamel(String.format("%s %s", "set", fieldName),
					StringHelper.ONE_OF_WHITESPACE_CHARS);
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

	@Override
	public Getter getGetter() {
		return getter;
	}

	@Override
	public Setter getSetter() {
		return setter;
	}

}
