/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.internal.util.MarkerObject;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

import adn.helpers.FunctionHelper.HandledSupplier;

/**
 * @author Ngoc Huy
 *
 */
public class FunctionalNoAccess
		extends AbstractLambdaPropertyAccess<Object, Object, Object, RuntimeException, Object, Object> {

	public static final FunctionalNoAccess INSTANCE = new FunctionalNoAccess();

	static final HandledSupplier<Object, RuntimeException> NO_OP = () -> new MarkerObject("FUNCTIONAL_NO_ACCESS");

	private FunctionalNoAccess() {
		super(NO_OP, NO_OP);
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.FUNCTIONAL_NO_ACCESS_STRATEGY;
	}

}
