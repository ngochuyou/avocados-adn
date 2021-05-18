/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import adn.helpers.FunctionHelper.HandledSupplier;
import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;

/**
 * @author Ngoc Huy
 *
 */
public class SupplyingPropertyAccess<R, E extends RuntimeException>
		extends AbstractLambdaPropertyAccess<Object, Object, R, E, HandledSupplier<R, E>, HandledSupplier<R, E>> {

	private final LambdaPropertyAccessStrategy<Object, Object, R, E, HandledSupplier<R, E>, HandledSupplier<R, E>, SupplyingPropertyAccess<R, E>> strategy;

	SupplyingPropertyAccess(HandledSupplier<R, E> getter, HandledSupplier<R, E> setter,
			LambdaPropertyAccessStrategy<Object, Object, R, E, HandledSupplier<R, E>, HandledSupplier<R, E>, SupplyingPropertyAccess<R, E>> strategy) {
		super(getter, setter);
		this.strategy = strategy;
	}

	@Override
	public LambdaPropertyAccessStrategy<Object, Object, R, E, HandledSupplier<R, E>, HandledSupplier<R, E>, SupplyingPropertyAccess<R, E>> getPropertyAccessStrategy() {
		return strategy;
	}

}
