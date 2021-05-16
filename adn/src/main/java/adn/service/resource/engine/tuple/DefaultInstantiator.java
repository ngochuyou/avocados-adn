/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.lang.reflect.InvocationTargetException;

import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class DefaultInstantiator<T> extends AbstractResourceInstantiator<T> {

	DefaultInstantiator() {}

	@Override
	public T instantiate() {
		try {
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		}
	}

	@Override
	public ResourceInstantiator<T> addColumnName(String name) {
		return this;
	}

}
