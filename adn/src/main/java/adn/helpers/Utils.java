/**
 * 
 */
package adn.helpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ngoc Huy
 *
 */
public class Utils {

	public static Date localDateToDate(LocalDate ldt) {
		return ldt == null ? null : Date.from(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate dateToLocalDate(Date date) {
		return date == null ? null : LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public static Date localDateTimeToDate(LocalDateTime ldt) {
		return ldt == null ? null : Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDateTime dateToLocalDateTime(Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
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
