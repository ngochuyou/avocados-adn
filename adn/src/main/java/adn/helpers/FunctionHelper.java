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

	public static <T> T doThrow(String message) {
		throw new IllegalArgumentException(message);
	}

	public static <T> T doThrow(Throwable ex) throws Throwable {
		throw ex;
	}

	public static <T, E extends Throwable> T doThrow(E ex, Class<E> type) throws E {
		throw ex;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HandledFunction from(HandledFunction target) {
		return target::apply;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HandledBiFunction from(HandledBiFunction target) {
		return target::apply;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HandledConsumer from(HandledConsumer target) {
		return target::accept;
	}

	@SuppressWarnings({ "rawtypes" })
	public static HandledSupplier from(HandledSupplier target) {
		return target::get;
	}

	@FunctionalInterface
	public static interface HandledFunction<T, R, E extends RuntimeException> {

		R apply(T one) throws E;

	}

	@FunctionalInterface
	public static interface HandledBiFunction<F, S, R, E extends RuntimeException> {

		R apply(F one, S two) throws E;

	}

	@FunctionalInterface
	public static interface HandledConsumer<T, E extends RuntimeException> {

		void accept(T one) throws E;

	}

	@FunctionalInterface
	public static interface HandledSupplier<R, E extends RuntimeException> {

		R get() throws E;

	}

}
