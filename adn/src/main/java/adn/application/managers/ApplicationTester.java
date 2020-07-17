/**
 * 
 */
package adn.application.managers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ApplicationManager {

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

}
