/**
 * 
 */
package adn.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

/**
 * @author Ngoc Huy
 *
 */
public class SpecificAccess extends AbstractPropertyAccess {

	SpecificAccess(Getter getter, Setter setter) {
		super(getter, setter);
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY;
	}

}
