/**
 * 
 */
package adn.helpers;

import java.util.Objects;

/**
 * @author Ngoc Huy
 *
 */
public class ArrayHelper {

	private ArrayHelper() {}

	public static <T> ArrayBuilder<T> from(T[] elements) {
		return elements == null ? FunctionHelper.doThrow("Cannot build Array from null") : new ArrayBuilder<>(elements);
	}

	public static class ArrayBuilder<T> {

		private final T[] array;

		private ArrayBuilder(T[] array) {
			this.array = array;
		}

		public boolean contains(T target) {
			for (T ele : array) {

				if (Objects.deepEquals(target, ele)) {
					return true;
				}
			}

			return false;
		}

		public ArrayBuilder<T> remove(T target) {
			int n = array.length;

			for (int i = 0; i < n; i++) {
				if (target.equals(array[i])) {
					System.arraycopy(array, i + 1, array, i, array.length - 1 - i);
					return this;
				}
			}

			return this;
		}

		public T[] get() {
			return array;
		}

	}

}
