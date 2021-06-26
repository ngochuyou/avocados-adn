/**
 * 
 */
package adn.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.persistence.Table;

import org.springframework.stereotype.Component;

import adn.model.AbstractModel;
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

	public static final Map<Class<?>, Set<Class<?>>> NON_PRIMITIVE_RELATION_MAP = Map.of(
		Integer.class, Set.of(int.class),
		Long.class, Set.of(int.class, long.class),
		Float.class, Set.of(float.class)
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

	public static Stack<Class<?>> getClassStack(Class<?> clazz) {
		Stack<Class<?>> stack = new Stack<>();
		Class<?> superClass = clazz;

		while (superClass != null && !superClass.equals(Object.class)) {
			stack.add(superClass);
			superClass = (Class<?>) superClass.getSuperclass();
		}

		return stack;
	}

	public static boolean isExtendedFrom(Class<?> clazz, Class<?> superClass) {
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

	public static boolean isImplementedFrom(Class<?> clazz, Class<?> superClass) {
		for (Class<?> i : clazz.getInterfaces()) {
			if (i.equals(superClass)) {
				return true;
			}
		}

		return false;
	}

	public static Type getGenericType(Class<?> clazz) {

		return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public static <M extends AbstractModel> M newModelOrAbstract(Class<M> clazz)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return clazz.getConstructor().newInstance();
	}

	public static void consumeFields(Object o, BiConsumer<Field, Object> consumer, boolean isSuperClassIncluded) {
		Stack<Class<?>> classStack = new Stack<>();

		classStack.add(o.getClass());

		if (isSuperClassIncluded) {
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
	public static <T, M extends T> M unwrap(T target) {
		return (M) target;
	}

	public static Field[] getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		Stack<Class<?>> classStack = getClassStack(type);

		while (!classStack.isEmpty()) {
			fields.addAll(Arrays.asList(classStack.pop().getDeclaredFields()));
		}

		return fields.toArray(new Field[fields.size()]);
	}

	public static boolean isParentOf(Class<?> possibleParent, Class<?> child) {
		Stack<Class<?>> classStack = getClassStack(child);

		while (!classStack.isEmpty()) {
			if (classStack.pop().equals(possibleParent)) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasSuperClass(Class<?> clz) {
		return clz.getSuperclass() != null && clz.getSuperclass() != Object.class;
	}

}
