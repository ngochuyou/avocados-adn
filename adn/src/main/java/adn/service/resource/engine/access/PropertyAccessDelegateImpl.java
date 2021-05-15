/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessDelegate;

public class PropertyAccessDelegateImpl extends AbstractPropertyAccess implements PropertyAccessDelegate {

	PropertyAccessDelegateImpl(Getter getter, Setter setter) {
		super(getter, setter);
	}

}