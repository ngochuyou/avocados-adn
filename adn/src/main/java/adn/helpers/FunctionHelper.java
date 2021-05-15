/**
 * 
 */
package adn.helpers;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author Ngoc Huy
 *
 */
public class FunctionHelper {

	private FunctionHelper() {}

	public static <T> T reject(String message) throws IllegalAccessException {
		throw new IllegalAccessException(message);
	}

	public static <T> T reject(Throwable ex) throws Throwable {
		throw ex;
	}

	public static <T, E extends Throwable> T reject(E ex, Class<E> type) throws E {
		throw ex;
	}

	public static Optional<Method> getMethod(Class<?> owner, String methodName, Class<?> paramTypes) {
		try {
			return Optional.of(owner.getDeclaredMethod(methodName, paramTypes));
		} catch (NoSuchMethodException | SecurityException e) {
			return Optional.ofNullable(null);
		}
	}

	@FunctionalInterface
	public static interface HandledFunction<T, R, E extends Throwable> {

		R apply(T arg) throws E;

	}

}
