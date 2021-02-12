/**
 * 
 */
package adn.service.resource.tuple;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Ngoc Huy
 *
 */
public class PlainInstanceInstantiator implements Instantiator {

	private final Constructor<?> constructor;

	private final Class<?> clazz;

	private final boolean embeddedIdentifier;
	private final boolean isAbstract;

	/**
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * 
	 */
	public PlainInstanceInstantiator(Class<?> clazz, boolean embedded, boolean isAbstract)
			throws NoSuchMethodException, SecurityException {
		// TODO Auto-generated constructor stub
		this.constructor = clazz.getConstructor();
		this.clazz = clazz;
		this.embeddedIdentifier = embedded;
		this.isAbstract = isAbstract;
	}

	@Override
	public Object instantiate(Serializable id)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		if (isAbstract) {
			throw new IllegalAccessException("Cannot instantiate abstract type");
		}

		if (embeddedIdentifier) {
			if (id != null) {
				return id;
			}
		}

		if (constructor != null) {
			return constructor.newInstance();
		}

		throw new IllegalStateException("Unable to locate Contructor for type: " + clazz);
	}

	@Override
	public boolean isInstance(Object object) {
		// TODO Auto-generated method stub
		return clazz.isInstance(object);
	}

}
