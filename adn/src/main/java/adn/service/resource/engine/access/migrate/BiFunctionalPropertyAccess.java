/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;

/**
 * @author Ngoc Huy
 *
 */
public class BiFunctionalPropertyAccess<F, S, R, E extends RuntimeException>
		extends AbstractLambdaPropertyAccess<F, S, R, E, HandledBiFunction<F, S, R, E>, HandledBiFunction<F, S, R, E>> {

	private final LambdaPropertyAccessStrategy<F, S, R, E, HandledBiFunction<F, S, R, E>, HandledBiFunction<F, S, R, E>, BiFunctionalPropertyAccess<F, S, R, E>> strategy;

	BiFunctionalPropertyAccess(HandledBiFunction<F, S, R, E> getter, HandledBiFunction<F, S, R, E> setter,
			LambdaPropertyAccessStrategy<F, S, R, E, HandledBiFunction<F, S, R, E>, HandledBiFunction<F, S, R, E>, BiFunctionalPropertyAccess<F, S, R, E>> strategy) {
		super(getter, setter);
		this.strategy = strategy;
	}

	@Override
	public LambdaPropertyAccessStrategy<F, S, R, E, HandledBiFunction<F, S, R, E>, HandledBiFunction<F, S, R, E>, BiFunctionalPropertyAccess<F, S, R, E>> getPropertyAccessStrategy() {
		return strategy;
	}

}
