/**
 * 
 */
package adn.helpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ngoc Huy
 *
 */
public class Utils {

	public static String ldt(LocalDateTime ldt) {
		return ldt != null ? ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
	}

	public static String ld(LocalDate ld) {
		return ld != null ? ld.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
	}

	public static String localDateTime(LocalDateTime ldt) {
		return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	public static String localDate(LocalDate ld) {
		return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
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

	}

}
