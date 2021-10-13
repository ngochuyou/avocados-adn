/**
 * 
 */
package adn.helpers;

import static adn.application.Common.COMMON_LDT_FORMATTER;
import static adn.application.Common.COMMON_LD_FORMATTER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Ngoc Huy
 *
 */
public class Utils {

	public static String ldt(LocalDateTime ldt) {
		return ldt != null ? ldt.format(COMMON_LDT_FORMATTER) : null;
	}

	public static String ld(LocalDate ld) {
		return ld != null ? ld.format(COMMON_LD_FORMATTER) : null;
	}

	public static String localDateTime(LocalDateTime ldt) {
		return ldt.format(COMMON_LDT_FORMATTER);
	}

	public static String localDate(LocalDate ld) {
		return ld.format(COMMON_LD_FORMATTER);
	}

	public static class Entry<K, V> implements Map.Entry<K, V> {

		private K key;
		private V value;

		public Entry(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

		public static <K, V> Entry<K, V> entry(K key, V val) {
			Objects.requireNonNull(key);
			return new Entry<>(key, val);
		}
		
		public static <K, V> Entry<K, V> uncheckedEntry(K key, V val) {
			return new Entry<>(key, val);
		}

		public K getKey() {
			return key;
		}

		public void setKey(K key) {
			this.key = key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			V oldVal = this.value;

			this.value = value;

			return oldVal;
		}

		public <T> T map(BiFunction<K, V, T> mapper) {
			return mapper.apply(key, value);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(key, value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry<?, ?> other = (Entry<?, ?>) obj;
			return Objects.equals(key, other.key) && Objects.equals(value, other.value);
		}

	}

	public static class Wrapper<T> {

		T value;

		public Wrapper(T value) {
			super();
			this.value = value;
		}

		public T getValue() {
			return value;
		}
		
		public T getThenMap(Function<T, T> mapper) {
			T current = value;
			value = mapper.apply(value);
			
			return current;
		}

		public void setValue(T value) {
			this.value = value;
		}

	}

}
