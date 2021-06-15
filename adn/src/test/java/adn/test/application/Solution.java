/**
 * 
 */
package adn.test.application;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	public static final String mark;
	public static final String letter;
	public static final String ops;

	public static Pattern SAVE_PATTERN;

	static {
		mark = "\\?,\\.''\"\\s\\t\\n><\\=\\(\\)";
		letter = "\\w\\d_";
		ops = "(\\=|like|LIKE|is|IS|>|<)";
		// @formatter:off
		String regex = String.format(""
				+ "(insert|INSERT)\\s+(into|INTO)\\s+(?<templatename>[%s]+)\\s+"
				+ "\\((?<columns>[%s]+)\\)\\s+"
				+ "(values|VALUES)\\s+\\((?<values>[%s]+)\\)\\s?",
				letter,
				letter + mark,
				letter + mark);
		// @formatter:on
		SAVE_PATTERN = Pattern.compile(regex);
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, SQLException {
		Int val = new Int(0);
		
		for (int i = 0; i < 10; i++) {
			System.out.println(val.getThenPlusOne());
		}
	}
	
	private static class Int {
		
		private int val;

		Int(int val) {
			this.val = val;
		}
		
		int getThenPlusOne() {
			return this.val++;
		}
		
	}

	private final Map<String, Object> mutexMap = new HashMap<>();

	@Test
	public void test() {
		Thread save0 = new Thread() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName() + " executing save ngochuy.ou ");

				saveLock("ngochuy.ou");
			}
		};

		Thread save1 = new Thread() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName() + " executing update ngochuy.ou ");

				updateLock("duypham");
			}
		};

		Thread update0 = new Thread() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName() + " executing update ngochuy.ou ");

				updateLock("ngochuy.ou");
			}
		};

		Thread update1 = new Thread() {
			@Override
			public void run() {
				System.out.println(Thread.currentThread().getName() + " executing save ngochuy.ou ");

				saveLock("ngochuy.ou");
			}
		};

		save0.start();
		save1.start();
		update0.start();
		update1.start();
	}

	private Object obtainMutex(String name) {
		if (mutexMap.containsKey(name)) {
			return mutexMap.get(name);
		}

		Object mutex = new Object();

		mutexMap.put(name, mutex);

		return mutex;
	}

	private void saveLock(String name) {
		synchronized (obtainMutex(name)) {
			System.out.println("Save-locking " + name);

			System.out.println("Save-releashing " + name);
		}
	}

	private void updateLock(String name) {
		synchronized (obtainMutex(name)) {
			System.out.println("Update-locking " + name);

			System.out.println("Update-releashing " + name);
		}
	}

}
