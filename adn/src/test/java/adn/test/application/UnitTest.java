/**
 * 
 */
package adn.test.application;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ngoc Huy
 *
 */
public class UnitTest {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IOException {
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
		Iterator<Integer> iterator = list.iterator();
		int sum = iterator.next();
		
		while (iterator.hasNext()) {
			sum += iterator.next();
		}
		
		System.out.println(sum == list.stream().reduce(Integer::sum).get());
	}

}
