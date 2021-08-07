/**
 * 
 */
package adn.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

/**
 * Access of this type provides, at least and maximum, either {@link Getter} or
 * {@link Setter}
 * 
 * @author Ngoc Huy
 *
 */
abstract class AbstractDisabledPropertyAccess extends AbstractPropertyAccess {

	AbstractDisabledPropertyAccess(Getter getter, Setter setter) {
		super(getter, setter);

		if (hasGetter() && hasSetter()) {
			throw new IllegalArgumentException(
					String.format("Either [%s] or [%s] must be null", Getter.class.getName(), Setter.class.getName()));
		}

		if (!hasGetter() && !hasSetter()) {
			throw new IllegalArgumentException(String.format("Either [%s] or [%s] must be provided",
					Getter.class.getName(), Setter.class.getName()));
		}
	}

}