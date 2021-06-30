/**
 * 
 */
package adn.engine.access;

import adn.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;
import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class FunctionalPropertyAccess<T, R, E extends RuntimeException>
		extends AbstractLambdaPropertyAccess<T, Object, R, E, HandledFunction<T, R, E>, HandledFunction<T, R, E>> {

	private final LambdaPropertyAccessStrategy<T, Object, R, E, HandledFunction<T, R, E>, HandledFunction<T, R, E>, FunctionalPropertyAccess<T, R, E>> strategy;

	FunctionalPropertyAccess(HandledFunction<T, R, E> getter, HandledFunction<T, R, E> setter,
			LambdaPropertyAccessStrategy<T, Object, R, E, HandledFunction<T, R, E>, HandledFunction<T, R, E>, FunctionalPropertyAccess<T, R, E>> strategy) {
		super(getter, setter);
		this.strategy = strategy;
	}

	@Override
	public LambdaPropertyAccessStrategy<T, Object, R, E, HandledFunction<T, R, E>, HandledFunction<T, R, E>, FunctionalPropertyAccess<T, R, E>> getPropertyAccessStrategy() {
		return strategy;
	}

}
