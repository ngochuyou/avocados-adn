/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.helpers.Utils;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;

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

		Utils.Entry<Object, LambdaPropertyAccess.LambdaType> getterEntry = AbstractLambdaPropertyAccess
				.validateGetter(getterLambda);
		Utils.Entry<Object, LambdaPropertyAccess.LambdaType> setterEntry = AbstractLambdaPropertyAccess
				.validateSetter(setterLambda);

		this.getterLambda = getterEntry.key == null ? FunctionalNoAccess.NO_OP : getterEntry.key;
		this.setterLambda = setterEntry.key == null ? FunctionalNoAccess.NO_OP : setterEntry.key;
		this.getterType = getterEntry.value;
		this.setterType = setterEntry.value;
		this.strategy = strategy;
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return strategy;
	}

	@Override
	public Getter getGetter() {
		return super.getGetter();
	}

	@Override
	public Setter getSetter() {
		return super.getSetter();
	}

	@Override
	public Object getGetterLambda() {
		return getterLambda;
	}

	@Override
	public Object getSetterLambda() {
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

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s(\n"
				+ "\t\t\tgetter=[%s], setter=[%s]\n"
				+ "\t\t\tgetterFunction=[%s], setterFunction=[%s]\n\t\t)",
				this.getClass().getName(),
				getGetter(), getSetter(), getterLambda, setterLambda);
		// @formatter:on
	}

}