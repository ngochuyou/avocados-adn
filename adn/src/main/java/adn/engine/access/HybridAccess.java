/**
 * 
 */
package adn.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess;
import adn.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccessStrategy;
import adn.helpers.Utils;

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

		this.getterLambda = getterEntry.getKey() == null ? FunctionalNoAccess.NO_OP : getterEntry.getKey();
		this.setterLambda = setterEntry.getKey() == null ? FunctionalNoAccess.NO_OP : setterEntry.getKey();
		this.getterType = getterEntry.getValue();
		this.setterType = setterEntry.getValue();
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