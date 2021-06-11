/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.springframework.util.Assert;

import adn.service.resource.engine.tuple.InstantiatorFactory.ParameterizedInstantiator;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ParameterizedResourceInstantiator<T> extends AbstractResourceInstantiator<T>
		implements ParameterizedInstantiator<T> {

	ParameterizedResourceInstantiator() {}

	@Override
	public ParameterizedInstantiator<T> setClass(Class<T> type) {
		this.type = type;
		return this;
	}

	@Override
	public ParameterizedInstantiator<T> addColumnName(String name) {
		String[] paramNames = new String[this.columnNames.length + 1];

		System.arraycopy(this.columnNames, 0, paramNames, 0, this.columnNames.length);
		paramNames[this.columnNames.length] = name;
		this.columnNames = paramNames;

		return this;
	}

	@Override
	public ParameterizedInstantiator<T> setConstructor(Constructor<T> constructor) {
		super.setConstructor(constructor);
		return this;
	}

	@Override
	public T instantiate() {
		throw new IllegalArgumentException("Unable to construct with no arguments");
	}

	@Override
	public T instantiate(Object[] values) throws IllegalArgumentException {
		Assert.isTrue(values.length >= columnNames.length,
				String.format("Invalid amount of argument passed, expect [%s] argument(s)", columnNames.length));

		try {
			return constructor.newInstance(Arrays.copyOf(values, columnNames.length));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
