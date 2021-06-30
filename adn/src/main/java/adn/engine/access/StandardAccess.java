/**
 * 
 */
package adn.engine.access;

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
 * Access of this type provide access up to non-public field (requires
 * {@link Field} presence in owner type) of a type. Then access that field via
 * <b>camel-cased</b> style {@link Getter}/{@link Setter}
 * </p>
 * 
 * <pre>
 * private int amount;
 * private boolean legal;
 * 
 * public int getAmount() {
 * 	return amount;
 * }
 * 
 * public void setAmount(int a) {
 * 	this.amount = a;
 * }
 * 
 * public boolean isLegal() {
 * 	return this.legal;
 * }
 * 
 * public void setLegal(boolean legal) {
 * 	this.legal = legal;
 * }
 * 
 * </pre>
 * 
 * @author Ngoc Huy
 *
 */
public class StandardAccess extends AbstractPropertyAccess {

	StandardAccess(Class<?> ownerType, String fieldName) throws IllegalArgumentException {
		// @formatter:off
		this(locateGetter(ownerType, fieldName)
				.orElseThrow(() -> new IllegalArgumentException(String.format(
					"Unable to locate getter for field name [%s] in type [%s]", fieldName, ownerType.getName()))),
			locateSetter(ownerType, fieldName)
				.orElseThrow(() -> new IllegalArgumentException(String.format(
					"Unable to locate setter for field name [%s] in type [%s] with argument types [%s]",
					fieldName, ownerType.getName()))));
		// @formatter:on
	}

	private StandardAccess(Getter getter, Setter setter) {
		super(getter, setter);
	}

	public static Optional<Getter> locateGetter(Class<?> ownerType, String fieldName) {
		try {
			@SuppressWarnings("unused") // for field check
			Field field = ownerType.getDeclaredField(fieldName);
			Method getterMethod = bestGuessGetter(ownerType, fieldName);

			return Optional.of(new GetterMethodImpl(ownerType, fieldName, getterMethod));
		} catch (Exception e) {
			return Optional.ofNullable(null);
		}
	}

	public static Optional<Setter> locateSetter(Class<?> ownerType, String fieldName) {
		try {
			Field field = ownerType.getDeclaredField(fieldName);
			String setterName = StringHelper.toCamel(String.format("%s %s", "set", fieldName),
					StringHelper.ONE_OF_WHITESPACE_CHARS);
			Method setterMethod = bestGuessSetter(ownerType, field, setterName);

			return Optional.of(new SetterMethodImpl(ownerType, fieldName, setterMethod));
		} catch (Exception e) {
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.STANDARD_ACCESS_STRATEGY;
	}

}
