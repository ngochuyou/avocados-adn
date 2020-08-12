/**
 * 
 */
package adn.application;

import org.springframework.context.ApplicationContext;

import adn.utilities.ClassReflector;

/**
 * @author Ngoc Huy
 *
 */
public interface ApplicationManager {

	final ApplicationContext context = ApplicationContextProvider.getApplicationContext();

	final ClassReflector reflector = ApplicationContextProvider.getApplicationContext().getBean(ClassReflector.class);

	void initialize() throws Exception;

}
