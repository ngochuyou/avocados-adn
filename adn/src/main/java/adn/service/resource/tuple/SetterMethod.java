/**
 * 
 */
package adn.service.resource.tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ngoc Huy
 *
 */
public class SetterMethod implements Setter {

	private final Method method;

	/**
	 * 
	 */
	public SetterMethod(Method method) {
		// TODO Auto-generated constructor stub
		this.method = method;
	}

	@Override
	public void set(Object target, Object value)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		method.invoke(target, value);
	}

}
