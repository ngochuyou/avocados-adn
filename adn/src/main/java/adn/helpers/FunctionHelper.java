/**
 * 
 */
package adn.helpers;

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

}
