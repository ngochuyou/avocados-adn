/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import org.hibernate.internal.util.MarkerObject;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

/**
 * @author Ngoc Huy
 *
 */
public class FunctionalNoAccess
		extends AbstractLambdaPropertyAccess<Object, Object, Object, RuntimeException, Object, Object> {

	public static final FunctionalNoAccess INSTANCE = new FunctionalNoAccess();

	private static final MarkerObject NULL = new MarkerObject("NULL");

	static final Object NO_OP = NULL;

	private FunctionalNoAccess() {
		super(NO_OP, NO_OP);
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.FUNCTIONAL_NO_ACCESS_STRATEGY;
	}

}
