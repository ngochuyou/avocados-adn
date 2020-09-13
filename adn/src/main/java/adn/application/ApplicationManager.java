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

	final ApplicationContext context = ContextProvider.getApplicationContext();

	final ClassReflector reflector = ContextProvider.getApplicationContext().getBean(ClassReflector.class);

	void initialize() throws Exception;

}
