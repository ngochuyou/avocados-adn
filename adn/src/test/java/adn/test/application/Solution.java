/**
 * 
 */
package adn.test.application;

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		Field path = File.class.getDeclaredField("status");
		File file = new File("abc.jpg");

		path.set(file, "INVALID");
	}

}
