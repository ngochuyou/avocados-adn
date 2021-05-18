/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;

/**
 * @author Ngoc Huy
 *
 */
public class MixedLambdaPropertyAccess<E extends RuntimeException>
		extends AbstractLambdaPropertyAccess<Object, Object, Object, E, Object, Object> {

	private final LambdaPropertyAccessStrategy<Object, Object, Object, E, Object, Object, MixedLambdaPropertyAccess<E>> strategy;

	MixedLambdaPropertyAccess(Object getter, Object setter,
			LambdaPropertyAccessStrategy<Object, Object, Object, E, Object, Object, MixedLambdaPropertyAccess<E>> strategy) {
		super(getter, setter);
		this.strategy = strategy;
	}

	@Override
	public LambdaPropertyAccessStrategy<Object, Object, Object, E, Object, Object, MixedLambdaPropertyAccess<E>> getPropertyAccessStrategy() {
		return strategy;
	}

}
