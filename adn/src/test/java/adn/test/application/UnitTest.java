/**
 * 
 */
package adn.test.application;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Ngoc Huy
 *
 */
public class UnitTest {

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IOException {
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusDays(2);
		Duration diff = Duration.between(start, end);
		
		System.out.println(diff.toSeconds());
	}

}
