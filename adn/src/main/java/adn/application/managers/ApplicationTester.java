/**
 * 
 */
package adn.application.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.model.entities.Personnel;
import adn.service.GenericServiceExecutor;
import adn.service.GenericStrategy;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ApplicationManager {

	@Autowired
	private GenericServiceExecutor serviceExecutor;

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		long t = System.currentTimeMillis();
		Personnel p = new Personnel();

		p = serviceExecutor.execute(p, GenericStrategy.DEFAULT, GenericStrategy.INSERT, GenericStrategy.UPDATE);
		
		System.out.println(System.currentTimeMillis() - t);
	}

}
