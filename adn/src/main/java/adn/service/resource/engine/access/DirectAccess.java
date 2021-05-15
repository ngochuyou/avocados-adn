/**
 * 
 */
package adn.service.resource.engine.access;

import java.lang.reflect.Field;
import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterFieldImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterFieldImpl;

import javassist.Modifier;

/**
 * @author Ngoc Huy
 *
 */
public class DirectAccess extends AbstractPropertyAccess {

	DirectAccess(Class<?> owner, String fieldName) throws IllegalArgumentException {
		try {
			Field field = owner.getDeclaredField(fieldName);

			if (!Modifier.isPublic(field.getModifiers())) {
				throw new SecurityException(
						String.format("Unable directly access non-public field [%s#%s]", owner.getName(), fieldName));
			}

			getter = new GetterFieldImpl(owner, fieldName, field);
			setter = new SetterFieldImpl(owner, fieldName, field);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException(e);
		}
	}

	static Optional<Getter> locateGetter(Class<?> owner, String fieldName) {
		try {
			Field field = owner.getDeclaredField(fieldName);

			if (!Modifier.isPublic(field.getModifiers())) {
				throw new SecurityException(
						String.format("Unable directly access non-public field [%s#%s]", owner.getName(), fieldName));
			}

			return Optional.of(new GetterFieldImpl(owner, fieldName, field));
		} catch (NoSuchFieldException | SecurityException e) {
			return Optional.ofNullable(null);
		}
	}

	static Optional<Setter> locateSetter(Class<?> owner, String fieldName) {
		try {
			Field field = owner.getDeclaredField(fieldName);

			if (!Modifier.isPublic(field.getModifiers())) {
				throw new SecurityException(
						String.format("Unable directly access non-public field [%s#%s]", owner.getName(), fieldName));
			}

			return Optional.of(new SetterFieldImpl(owner, fieldName, field));
		} catch (NoSuchFieldException | SecurityException e) {
			return Optional.ofNullable(null);
		}
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.DIRECT_ACCESS_STRATEGY;
	}

	@Override
	public String toString() {
		return String.format("%s(fieldName=[%s])", this.getClass().getSimpleName(),
				((Field) getter.getMember()).getName());
	}

}
