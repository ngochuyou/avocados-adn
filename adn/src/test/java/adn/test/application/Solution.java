/**
 * 
 */
package adn.test.application;

import java.io.File;

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
		File file = new File("C:\\Users\\Ngoc Huy\\Pictures\\Saved Pictures\\IMG_20210301_162741.jpg");

		System.out.println(file.getPath());
		System.out.println(file.getName());
		System.out.println(Object.class.isAssignableFrom(long.class));
	}

}
