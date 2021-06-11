/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.tuple.Instantiator;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class InstantiatorFactory {

	private InstantiatorFactory() {}

	public static <T> PojoInstantiator<T> buildDefault(Class<T> type) throws IllegalArgumentException {
		try {
			return new DefaultInstantiator<T>().setConstructor(type.getConstructor()).setClass(type);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(
					String.format("Unable to locate default constructor for [%s]", type.getName()));
		}
	}

	public static <T> PojoInstantiator<T> build(Class<T> type, String[] paramNames, Class<?>[] paramTypes)
			throws IllegalArgumentException {
		Assert.isTrue(paramNames.length == paramTypes.length,
				"Amount of parameter names and parameter types must match");

		ParameterizedInstantiator<T> builder;

		try {
			builder = new ParameterizedResourceInstantiator<T>().setConstructor(type.getConstructor(paramTypes));

			for (String name : paramNames) {
				builder = builder.addColumnName(name);
			}

			return builder.setClass(type);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(String.format(
					"Unable to locate parameterized constructor for [%s] with argument types [%s]", type.getName(),
					Stream.of(paramTypes).map(String::valueOf).collect(Collectors.joining(", "))));
		}
	}

	public interface PojoInstantiator<T> extends Instantiator {

		PojoInstantiator<T> setConstructor(Constructor<T> constructor) throws IllegalArgumentException;

		PojoInstantiator<T> setClass(Class<T> clazz);

		PojoInstantiator<T> addColumnName(String name);

		String[] getParameterNames();

		@Override
		T instantiate();

		@Override
		default T instantiate(Serializable id) {
			return instantiate();
		}

		Class<?>[] getParameterTypes();

	}

	public interface ParameterizedInstantiator<T> extends PojoInstantiator<T> {

		T instantiate(Object[] values);

		@Override
		default T instantiate(Serializable id) {
			return instantiate(new Object[] { id });
		}

		@Override
		ParameterizedInstantiator<T> addColumnName(String name);

	}

}
