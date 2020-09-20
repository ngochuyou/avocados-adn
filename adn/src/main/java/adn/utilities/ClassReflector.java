/**
 * 
 */
package adn.utilities;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Stack;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import adn.model.AbstractModel;

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

	@SuppressWarnings("unchecked")
	public <M> M newInstanceOrAbstract(Class<M> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return (M) new AbstractModel() {
				
				@Override
				public Serializable getId() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
	}

	public void consumeFields(Object o, BiConsumer<Field, Object> consumer, boolean superClassIncluded) {
		Stack<Class<?>> classStack = new Stack<>();

		classStack.add(o.getClass());

		if (superClassIncluded) {
			classStack.addAll(getClassStack(o.getClass().getSuperclass()));
		}

		while (!classStack.isEmpty()) {
			for (Field f : classStack.pop().getDeclaredFields()) {
				f.setAccessible(true);
				consumer.accept(f, o);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T, M extends T> M genericallyCast(T target) {

		return (M) target;
	}

}
