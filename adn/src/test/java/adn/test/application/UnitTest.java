/**
 * 
 */
package adn.test.application;

import java.io.IOException;
import java.math.BigInteger;

import adn.helpers.Base32;

/**
 * @author Ngoc Huy
 *
 */
public class UnitTest {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IOException {
		long t1 = System.currentTimeMillis();

		for (int i = 10; i < 900; i++) {
			System.out.println(i + " " + Base32.crockfords.format(new BigInteger(String.valueOf(i))));
		}

		System.out.println(System.currentTimeMillis() - t1);
	}

}
