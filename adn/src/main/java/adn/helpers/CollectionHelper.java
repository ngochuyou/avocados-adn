/**
 * 
 */
package adn.helpers;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public class CollectionHelper {

	private CollectionHelper() {}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static final String[] from(Collection<String> elements) {
		return elements.toArray(String[]::new);
	}

	@SuppressWarnings("unchecked")
	public static <E, C extends Collection<E>> E[] from(C collection, Class<E> type) {
		return collection.toArray((E[]) Array.newInstance(type, collection.size()));
	}

	@SuppressWarnings("unchecked")
	public static <E> E[] from(E[] elements, Class<E> type) {
		return elements == null ? (E[]) Array.newInstance(type, 0) : elements;
	}

	public static <T> ArrayBuilder<T> from(T[] elements) {
		return elements == null ? FunctionHelper.doThrow("Cannot build Array from null") : new ArrayBuilder<>(elements);
	}

	/**
	 * @return A pair of value where key is the result String collection and value
	 *         determines whether the result array differs from the input collection
	 *         in terms of elements presence
	 */
	public static Map.Entry<Collection<String>, Boolean> appendIfAbsent(Collection<String> elements, String target) {
		Set<String> set = new HashSet<>(elements);

		if (set.contains(target)) {
			return Map.entry(set, Boolean.FALSE);
		}

		set.add(target);
		return Map.entry(set, Boolean.TRUE);
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