/**
 * 
 */
package adn.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import javax.persistence.Table;

import org.springframework.stereotype.Component;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class TypeHelper {
	private TypeHelper() {};

	// @formatter:off
	public static final Map<Class<?>, Map<Class<?>, Function<Object, Object>>> TYPE_CONVERTER;
	
	static {
		Map<Class<?>, Function<Object, Object>> dateConverters = Map.of(
				Long.class, (longVal) -> new Date((Long) longVal),
				long.class, (longVal) -> new Date((long) longVal),
				Date.class, (javaDate) -> new java.util.Date(((Date) javaDate).getTime()),
				java.util.Date.class, (sqlDate) -> new Date(((java.util.Date) sqlDate).getTime()),
				Timestamp.class, (sqlTimestamp) -> new Date(((Timestamp) sqlTimestamp).getTime())
		);
		
		TYPE_CONVERTER = Map.of(
				Timestamp.class, Map.of(
						Long.class, (longVal) -> new Timestamp((Long) longVal),
						Date.class, (date) -> new Timestamp(((Date) date).getTime())
				),
				Date.class, dateConverters,
				java.util.Date.class, dateConverters,
				long.class, Map.of(
						Timestamp.class, (stamp) -> ((Timestamp) stamp).getTime()
				),
				int.class, Map.of(
						Integer.class, (nonPrim) -> ((Integer) nonPrim).intValue(),
						Long.class, (nonPrim) -> Long.valueOf((long) nonPrim).intValue()
				),
				Integer.class, Map.of(
						int.class, (nonPrim) -> (int) nonPrim
				)
		);
	}

	public static final Map<Class<?>, Set<Class<?>>> RELATION_MAP = Map.of(
			Boolean.class, Set.of(boolean.class),
			Character.class, Set.of(char.class),
			Byte.class, Set.of(byte.class),
			Short.class, Set.of(short.class, byte.class),
			Integer.class, Set.of(int.class, short.class, byte.class),
			Double.class, Set.of(double.class, float.class, int.class, short.class, byte.class),
			Long.class, Set.of(long.class, double.class, float.class, int.class, short.class, byte.class),
			Float.class, Set.of(float.class, long.class, int.class, short.class, byte.class)
	);
	// @formatter:on
	public static <T extends Entity> String getEntityName(Class<T> clazz) {
		javax.persistence.Entity anno = clazz.getDeclaredAnnotation(javax.persistence.Entity.class);

		if (anno == null || !StringHelper.hasLength(anno.name())) {
			return clazz.getSimpleName();
		}

		return anno.name();
	}

	public static <T extends Entity> String getTableName(Class<T> clazz) {
		Table anno = clazz.getDeclaredAnnotation(Table.class);

		if (anno == null || !StringHelper.hasLength(anno.name())) {
			return clazz.getSimpleName();
		}

		return anno.name();
	}

	public static String getComponentName(Class<?> clazz) {
		Component anno = clazz.getDeclaredAnnotation(Component.class);

		if (anno == null || !StringHelper.hasLength(anno.value())) {
			return StringHelper.toCamel(clazz.getSimpleName(), null);
		}

		return anno.value();
	}

	public static <T> Stack<Class<? super T>> getClassStack(Class<T> clazz) {
		Stack<Class<? super T>> stack = new Stack<>();
		Class<? super T> superClass = clazz;

		while (superClass != null && !superClass.equals(Object.class)) {
			stack.add(superClass);
			superClass = (Class<? super T>) superClass.getSuperclass();
		}

		return stack;
	}

	public static <T> Stack<Class<? super T>> getClassStack(Class<T> clazz, Class<? super T> expectedParent) {
		Stack<Class<? super T>> stack = new Stack<>();
		Class<? super T> superClass = clazz;

		while (superClass != null && !superClass.equals(expectedParent)) {
			stack.add(superClass);
			superClass = (Class<? super T>) superClass.getSuperclass();
		}

		return stack;
	}

	public static boolean isExtendedFrom(Class<?> clazz, Class<?> superClass) {
		if (clazz.equals(superClass)) {
			return true;
		}

		Class<?> root = clazz;

		while ((root = root.getSuperclass()) != null) {
			if (root.equals(superClass)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isImplementedFrom(Class<?> clazz, Class<?> superClass) {
		for (Class<?> i : clazz.getInterfaces()) {
			if (i.equals(superClass)) {
				return true;
			}
		}

		return false;
	}

	public static Type getGenericType(Collection<?> collection) {
		for (Object o : collection) {
			if (o != null) {
				return o.getClass();
			}
		}

		return null;
	}

	public static Type getGenericType(Field field) {
		return ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
	}

	public static boolean isParentOf(Class<?> possibleParent, Class<?> child) {
		Stack<?> classStack = getClassStack(child);

		while (!classStack.isEmpty()) {
			if (classStack.pop().equals(possibleParent)) {
				return true;
			}
		}

		return false;
	}

}
