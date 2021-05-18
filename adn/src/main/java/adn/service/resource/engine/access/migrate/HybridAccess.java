/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import java.util.Map;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.LambdaPropertyAccess;
import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;

public class HybridAccess<F, S, R, E extends RuntimeException> extends AbstractPropertyAccess
		implements LambdaPropertyAccess<F, S, R, E, Object, Object> {

	private final Object getterLambda;
	private final Object setterLambda;

	private final LambdaPropertyAccess.LambdaType getterType;
	private final LambdaPropertyAccess.LambdaType setterType;

	private final LambdaPropertyAccessStrategy<F, S, R, E, Object, Object, HybridAccess<F, S, R, E>> strategy;

	HybridAccess(Getter getter, Setter setter, Object getterLambda, Object setterLambda,
			LambdaPropertyAccessStrategy<F, S, R, E, Object, Object, HybridAccess<F, S, R, E>> strategy) {
		super(getter, setter);

		Map.Entry<Object, LambdaPropertyAccess.LambdaType> getterEntry = AbstractLambdaPropertyAccess
				.validateGetter(getterLambda);
		Map.Entry<Object, LambdaPropertyAccess.LambdaType> setterEntry = AbstractLambdaPropertyAccess
				.validateSetter(setterLambda);

		this.getterLambda = getterEntry.getKey();
		this.setterLambda = setterEntry.getKey();
		this.getterType = getterEntry.getValue();
		this.setterType = setterEntry.getValue();
		this.strategy = strategy;
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return strategy;
	}

	@Override
	public Object getGetterFunction() {
		return getterLambda;
	}

	@Override
	public Object getSetterFunction() {
		return setterLambda;
	}

	@Override
	public LambdaPropertyAccess.LambdaType getGetterType() {
		return getterType;
	}

	@Override
	public LambdaPropertyAccess.LambdaType getSetterType() {
		return setterType;
	}

}