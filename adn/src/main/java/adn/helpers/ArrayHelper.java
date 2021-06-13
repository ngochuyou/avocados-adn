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

	/**
	 * Use for cases which performance matter is not crucial
	 *
	 * @param <T>
	 */
	public static class ArrayBuilder<T> {

		private final T[] array;
		private T lastFound = null;
		private int lastFoundIndex = -1;
		private final int size;

		private ArrayBuilder(T[] array) {
			this.array = array;
			size = array.length;
		}

		public boolean contains(T target) {
			T ele;

			for (int i = 0; i < size; i++) {
				ele = array[i];

				if (Objects.deepEquals(target, ele)) {
					lastFound = ele;
					lastFoundIndex = i;
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

		public T getLastFound() {
			return lastFound;
		}

		public int getLastFoundIndex() {
			return lastFoundIndex;
		}

	}

}
