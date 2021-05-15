/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.property.access.spi.PropertyAccessStrategy;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class FunctionalPropertyAccessImpl<T, R, E extends Throwable> extends AbstractFunctionalPropertyAccess<T, R, E> {

	FunctionalPropertyAccessImpl(HandledFunction<T, R, E> getter, HandledFunction<T, R, E> setter) {
		super(null, null, getter, setter);
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.FUNCTIONAL_ACCESS_STRATEGY;
	}
	
}
