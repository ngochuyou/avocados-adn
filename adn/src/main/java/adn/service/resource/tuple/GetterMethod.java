/**
 * 
 */
package adn.service.resource.tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * @author Ngoc Huy
 *
 */
public class GetterMethod<T> implements Getter {

	private final Method method;

	private Class<T> returnType;

	/**
	 * 
	 */
	public GetterMethod(Class<T> returnType, Method method) {
		// TODO Auto-generated constructor stub
		this.returnType = returnType;
		this.method = method;
	}

	@Override
	public Object get(Object o) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		return method.invoke(o);
	}

	@Override
	public Class<?> getReturnType() {
		// TODO Auto-generated method stub
		return returnType;
	}

	@Override
	public Member getMember() {
		// TODO Auto-generated method stub
		return method;
	}

}
