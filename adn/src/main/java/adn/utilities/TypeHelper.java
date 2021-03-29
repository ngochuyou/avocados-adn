/**
 * 
 */
package adn.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;

import javax.persistence.Table;

import org.springframework.stereotype.Component;

import adn.model.entities.Entity;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class TypeHelper {

	private TypeHelper() {};

	public static <T extends Entity> String getEntityName(Class<T> clazz) {
		javax.persistence.Entity anno = clazz.getDeclaredAnnotation(javax.persistence.Entity.class);

		if (anno == null || !Strings.hasLength(anno.name())) {
			return clazz.getSimpleName();
		}

		return anno.name();
	}

	public static <T extends Entity> String getTableName(Class<T> clazz) {
		Table anno = clazz.getDeclaredAnnotation(Table.class);

		if (anno == null || !Strings.hasLength(anno.name())) {
			return clazz.getSimpleName();
		}

		return anno.name();
	}

	public static String getComponentName(Class<?> clazz) {
		Component anno = clazz.getDeclaredAnnotation(Component.class);

		if (anno == null || !Strings.hasLength(anno.value())) {
			return Strings.toCamel(clazz.getSimpleName(), null);
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

	@SuppressWarnings("unchecked")
	public static <M extends Model> M newModelOrAbstract(Class<M> clazz) {
		try {
			return clazz.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return (M) new Model() {

				@Override
				public String getId() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
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
	public static <T, M extends T> M cast(T target) {

		return (M) target;
	}

	public static <T extends Serializable> byte[] serialize(T instance) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream dos = new ObjectOutputStream(baos);

		dos.writeObject(instance);
		dos.close();

		return baos.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] serialized, Class<T> clazz) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized));

		return (T) ois.readObject();
	}

	public static Field[] getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		Stack<Class<?>> classStack = getClassStack(type);

		while (!classStack.isEmpty()) {
			fields.addAll(Arrays.asList(classStack.pop().getDeclaredFields()));
		}

		return fields.toArray(new Field[fields.size()]);
	}

}
