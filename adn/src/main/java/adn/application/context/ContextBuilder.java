/**
 * 
 */
package adn.application.context;

import org.springframework.context.ApplicationContext;

import adn.utilities.ClassReflector;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuilder {

	final ApplicationContext context = ContextProvider.getApplicationContext();

	final ClassReflector reflector = ContextProvider.getApplicationContext().getBean(ClassReflector.class);

	void initialize() throws Exception;

}
