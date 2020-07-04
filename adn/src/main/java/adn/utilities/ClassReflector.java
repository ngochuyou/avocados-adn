/**
 * 
 */
package adn.utilities;

import java.util.Stack;

import org.springframework.stereotype.Component;

import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ClassReflector {

	@SuppressWarnings("unchecked")
	public Stack<Class<? extends Model>> getModelClassStack(Class<? extends Model> clazz) {
		Stack<Class<? extends Model>> stack = new Stack<>();
		Class<? extends Model> superClass = clazz;

		while (superClass != null && !superClass.equals(Object.class)) {
			stack.add(superClass);
			superClass = (Class<? extends Model>) superClass.getSuperclass();
		}

		return stack;
	}

	public boolean isExtendedFrom(Class<?> clazz, Class<?> superClass) {
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

}
