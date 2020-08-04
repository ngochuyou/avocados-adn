/**
 * 
 */
package adn.application.managers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import adn.application.ApplicationManager;

/**
 * @author Ngoc Huy
 *
 */
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ApplicationManager {

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

}
