/**
 * 
 */
package adn.utilities;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Stack;

import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ClassReflector {

	public Stack<Class<?>> getClassStack(Class<?> clazz) {
		Stack<Class<?>> stack = new Stack<>();
		Class<?> superClass = clazz;

		while (superClass != null && !superClass.equals(Object.class)) {
			stack.add(superClass);
			superClass = (Class<?>) superClass.getSuperclass();
		}

		return stack;
	}

	public boolean isExtendedFrom(Class<?> clazz, Class<?> superClass) {
		if (clazz.equals(superClass)) {
			return true;
		}

		while ((clazz = clazz.getSuperclass()) != null) {
			if (clazz.equals(superClass)) {
				return true;
			}
		}

		return false;
	}

	public boolean isImplementedFrom(Class<?> clazz, Class<?> superClass) {
		for (Class<?> i : clazz.getInterfaces()) {
			if (i.equals(superClass)) {
				return true;
			}
		}

		return false;
	}

	public Type getGenericType(Class<?> clazz) {

		return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

}
