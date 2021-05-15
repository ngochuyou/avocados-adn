/**
 * 
 */
package adn.service.resource.engine.access;

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
public class LiterallyNamedAccess extends AbstractPropertyAccess implements PropertyAccessDelegate {

	LiterallyNamedAccess(Class<?> owner, String fieldName, Class<?> paramType) {
		this.getter = locateGetter(owner, fieldName).orElse(NoAccess.NO_OP_GETTER);

		if (getter != NoAccess.NO_OP_GETTER) {
			setter = NoAccess.NO_OP_SETTER;
			return;
		}

		this.setter = locateSetter(owner, fieldName, paramType).orElseThrow(() -> new IllegalArgumentException(
				String.format("Unable to locate neither getter nor setter access of [%s] for [%s#%s]",
						LiterallyNamedAccess.class.getName(), owner.getName(), fieldName)));
	}

	static Optional<Getter> locateGetter(Class<?> owner, String fieldName_aka_methodname) {
		try {
			Method method = owner.getDeclaredMethod(fieldName_aka_methodname);

			return Optional.of(new GetterMethodImpl(owner, fieldName_aka_methodname, method));
		} catch (SecurityException | NoSuchMethodException e) {
			return Optional.ofNullable(null);
		}
	}

	static Optional<Setter> locateSetter(Class<?> owner, String fieldName_aka_methodname, Class<?> paramType) {
		try {
			Method method = owner.getDeclaredMethod(fieldName_aka_methodname, paramType);

			return Optional.of(new SetterMethodImpl(owner, fieldName_aka_methodname, method));
		} catch (SecurityException | NoSuchMethodException e) {
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.LITERALLY_NAMED_ACCESS_STRATEGY;
	}

}
