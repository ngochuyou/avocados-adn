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

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessDelegate;

/**
 * @author Ngoc Huy
 *
 */
public class LiterallyNamedAccess implements PropertyAccessDelegate {

	private final Getter getter;
	private final Setter setter;

	LiterallyNamedAccess(Class<?> owner, String fieldName) {
		this.getter = locateGetter(owner, fieldName).orElse(NoAccess.NO_OP_GETTER);

		if (getter != NoAccess.NO_OP_GETTER) {
			setter = NoAccess.NO_OP_SETTER;
			return;
		}

		this.setter = locateSetter(owner, fieldName).orElseThrow(() -> new IllegalArgumentException(
				String.format("Unable to locate neither getter nor setter access of [%s] for [%s#%s]",
						LiterallyNamedAccess.class.getName(), owner.getName(), fieldName)));
	}

	static Optional<Getter> locateGetter(Class<?> owner, String fieldName_aka_methodname) {
		try {
			@SuppressWarnings("unused") // for exception catching only
			Field field = owner.getDeclaredField(fieldName_aka_methodname);
			Method method = owner.getDeclaredMethod(fieldName_aka_methodname);

			return Optional.of(new GetterMethodImpl(owner, fieldName_aka_methodname, method));
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			return Optional.ofNullable(null);
		}
	}

	static Optional<Setter> locateSetter(Class<?> owner, String fieldName_aka_methodname) {
		try {
			Field field = owner.getDeclaredField(fieldName_aka_methodname);
			Method method = owner.getDeclaredMethod(fieldName_aka_methodname, field.getType());

			return Optional.of(new SetterMethodImpl(owner, fieldName_aka_methodname, method));
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.LITERALLY_NAMED_ACCESS_STRATEGY;
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
