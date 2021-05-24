/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.property.access.spi.PropertyAccessStrategy;

import adn.helpers.FunctionHelper.HandledConsumer;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;

/**
 * @author Ngoc Huy
 *
 */
public class ConsumingPropertyAccess<T, E extends RuntimeException>
		extends AbstractLambdaPropertyAccess<T, Object, Object, E, HandledConsumer<T, E>, HandledConsumer<T, E>> {

	private final LambdaPropertyAccessStrategy<T, Object, Object, E, HandledConsumer<T, E>, HandledConsumer<T, E>, ConsumingPropertyAccess<T, E>> strategy;

	ConsumingPropertyAccess(HandledConsumer<T, E> getter, HandledConsumer<T, E> setter,
			LambdaPropertyAccessStrategy<T, Object, Object, E, HandledConsumer<T, E>, HandledConsumer<T, E>, ConsumingPropertyAccess<T, E>> strategy) {
		super(getter, setter);
		this.strategy = strategy;
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return strategy;
	}

}
