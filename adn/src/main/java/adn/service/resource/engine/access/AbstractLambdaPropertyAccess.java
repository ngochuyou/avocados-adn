/**
 * 
 */
package adn.service.resource.engine.access;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;
import adn.helpers.Utils;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractLambdaPropertyAccess<F, S, R, E extends RuntimeException, GETTER, SETTER>
		implements LambdaPropertyAccess<F, S, R, E, GETTER, SETTER> {

	private final GETTER getter;
	private final SETTER setter;

	private final LambdaType getterType;
	private final LambdaType setterType;

	@SuppressWarnings("unchecked")
	AbstractLambdaPropertyAccess(GETTER getter, SETTER setter) {
		Utils.Entry<GETTER, LambdaType> getterEntry = validateGetter(getter);
		Utils.Entry<SETTER, LambdaType> setterEntry = validateSetter(setter);

		this.getter = getterEntry.key == null ? (GETTER) FunctionalNoAccess.NO_OP : getterEntry.key;
		this.setter = setterEntry.key == null ? (SETTER) FunctionalNoAccess.NO_OP : setterEntry.key;
		this.getterType = getterEntry.value;
		this.setterType = setterEntry.value;
	}

	static <GETTER> Utils.Entry<GETTER, LambdaType> validateGetter(GETTER getter) {
		if (getter == null) {
			return new Utils.Entry<>(null, LambdaType.NO_ACCESS);
		}

		if (getter instanceof HandledSupplier) {
			return new Utils.Entry<>(getter, LambdaType.SUPPLIER);
		}

		if (getter instanceof HandledFunction) {
			return new Utils.Entry<>(getter, LambdaType.FUNCTION);
		}

		if (getter instanceof HandledBiFunction) {
			return new Utils.Entry<>(getter, LambdaType.BIFUNCTION);
		}

		throw new IllegalArgumentException(String.format("Unknown getter type [%s]", getter.getClass().getName()));
	}

	static <SETTER> Utils.Entry<SETTER, LambdaType> validateSetter(SETTER setter) {
		if (setter == null) {
			return new Utils.Entry<>(null, LambdaType.NO_ACCESS);
		}

		if (setter instanceof HandledConsumer) {
			return new Utils.Entry<>(setter, LambdaType.CONSUMER);
		}

		if (setter instanceof HandledSupplier) {
			return new Utils.Entry<>(setter, LambdaType.SUPPLIER);
		}

		if (setter instanceof HandledFunction) {
			return new Utils.Entry<>(setter, LambdaType.FUNCTION);
		}

		if (setter instanceof HandledBiFunction) {
			return new Utils.Entry<>(setter, LambdaType.BIFUNCTION);
		}

		throw new IllegalArgumentException(String.format("Unknown setter type [%s]", setter.getClass().getName()));
	}

	@Override
	public GETTER getGetterFunction() {
		return getter;
	}

	@Override
	public SETTER getSetterFunction() {
		return setter;
	}

	@Override
	public LambdaType getGetterType() {
		return getterType;
	}

	@Override
	public LambdaType getSetterType() {
		return setterType;
	}

	@Override
	public String toString() {
		return String.format("%s(getter=[%s], setter=[%s])", this.getClass().getSimpleName(), getter, setter);
	}

}