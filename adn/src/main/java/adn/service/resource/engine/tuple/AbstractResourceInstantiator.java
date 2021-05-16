/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.lang.reflect.Constructor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractResourceInstantiator<T> implements ResourceInstantiator<T> {

	protected Class<T> type;
	protected Constructor<T> constructor;

	protected String[] columnNames = new String[0];

	AbstractResourceInstantiator() {}

	@Override
	public ResourceInstantiator<T> setClass(Class<T> type) {
		this.type = type;
		return null;
	}

	@Override
	public ResourceInstantiator<T> setConstructor(Constructor<T> constructor) throws IllegalArgumentException {
		if (constructor == null) {
			throw new IllegalArgumentException(
					String.format("Unable to set [%s], provided constructor is [NULL]", Constructor.class.getName()));
		}

		this.constructor = constructor;
		return this;
	}

	@Override
	public boolean isInstance(Object object) {
		return object.getClass().isAssignableFrom(type);
	}

	@Override
	public String[] getParameterNames() {
		return columnNames;
	}

	@Override
	public String toString() {
		return String.format("%s(type=[%s], paramNames=[%s])", this.getClass().getSimpleName(), type.getName(),
				Stream.of(columnNames).collect(Collectors.joining(", ")));
	}

}
