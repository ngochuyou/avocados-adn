/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import java.util.Map;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;
import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.LambdaPropertyAccess;

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

	AbstractLambdaPropertyAccess(GETTER getter, SETTER setter) {
		Map.Entry<GETTER, LambdaType> getterEntry = validateGetter(getter);
		Map.Entry<SETTER, LambdaType> setterEntry = validateSetter(setter);

		this.getter = getterEntry.getKey();
		this.setter = setterEntry.getKey();
		this.getterType = getterEntry.getValue();
		this.setterType = setterEntry.getValue();
	}

	static <GETTER> Map.Entry<GETTER, LambdaType> validateGetter(GETTER getter) {
		if (getter == null) {
			return Map.entry(null, LambdaType.NO_ACCESS);
		}

		if (getter instanceof HandledSupplier) {
			return Map.entry(getter, LambdaType.SUPPLIER);
		}

		if (getter instanceof HandledFunction) {
			return Map.entry(getter, LambdaType.FUNCTION);
		}

		if (getter instanceof HandledBiFunction) {
			return Map.entry(getter, LambdaType.BIFUNCTION);
		}

		throw new IllegalArgumentException(String.format("Unknown getter type [%s]", getter.getClass().getName()));
	}

	static <SETTER> Map.Entry<SETTER, LambdaType> validateSetter(SETTER setter) {
		if (setter == null) {
			return Map.entry(null, LambdaType.NO_ACCESS);
		}

		if (setter instanceof HandledConsumer) {
			return Map.entry(setter, LambdaType.CONSUMER);
		}

		if (setter instanceof HandledSupplier) {
			return Map.entry(setter, LambdaType.SUPPLIER);
		}

		if (setter instanceof HandledFunction) {
			return Map.entry(setter, LambdaType.FUNCTION);
		}

		if (setter instanceof HandledBiFunction) {
			return Map.entry(setter, LambdaType.BIFUNCTION);
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

}
