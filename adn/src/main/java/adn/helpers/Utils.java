/**
 * 
 */
package adn.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Ngoc Huy
 *
 */
public class Utils {

	public static Date localDateTimeToDate(LocalDateTime ldt) {
		return ldt == null ? null : Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDateTime dateToLocalDateTime(Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public static class Entry<K, V> {

		public K key;
		public V value;

		public Entry(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

	}

}
