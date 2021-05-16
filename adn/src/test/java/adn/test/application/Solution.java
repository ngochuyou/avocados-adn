/**
 * 
 */
package adn.test.application;

import java.io.File;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

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
		LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

		System.out.println(discoverer.getParameterNames(File.class.getConstructor(String.class))[0]);
	}

}
