/**
 * 
 */
package adn.service.resource.engine.access;

import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

import adn.helpers.FunctionHelper.HandledFunction;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.FunctionalPropertyAccess;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractFunctionalPropertyAccess<T, R, E extends Throwable> extends AbstractPropertyAccess
		implements FunctionalPropertyAccess<T, R, E> {

	protected HandledFunction<T, R, E> getterFnc;
	protected HandledFunction<T, R, E> setterFnc;

	AbstractFunctionalPropertyAccess() {}

	AbstractFunctionalPropertyAccess(Getter getter, Setter setter, HandledFunction<T, R, E> getterFnc,
			HandledFunction<T, R, E> setterFnc) {
		super(getter, setter);
		this.getterFnc = getFunctionOrNoOp(getterFnc);
		this.setterFnc = getFunctionOrNoOp(setterFnc);
	}

	@Override
	public HandledFunction<T, R, E> getGetterFunction() {
		return getterFnc;
	}

	@Override
	public HandledFunction<T, R, E> getSetterFunction() {
		return setterFnc;
	}

	@Override
	public FunctionalPropertyAccess<T, R, E> setGetterFunction(HandledFunction<T, R, E> getter) {
		this.getterFnc = getFunctionOrNoOp(getter);
		return this;
	}

	@Override
	public FunctionalPropertyAccess<T, R, E> setSetterFunction(HandledFunction<T, R, E> setter) {
		this.setterFnc = getFunctionOrNoOp(setter);
		return this;
	}

	@SuppressWarnings("unchecked")
	private HandledFunction<T, R, E> getFunctionOrNoOp(HandledFunction<T, R, E> instance) {
		return Optional.ofNullable(instance).orElse(NO_OP_FNC);
	}

	@Override
	public String toString() {
		return String.format("%s(getter=[%s], setter=[%s], getterFnc=[%s], setterFnc=[%s])",
				this.getClass().getSimpleName(), getter.toString(), setter.toString(), getterFnc, setterFnc);
	}

}
