/**
 * 
 */
package adn.application.context;

import org.springframework.context.ApplicationContext;

import adn.utilities.TypeUtils;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuilder {

	final ApplicationContext context = ContextProvider.getApplicationContext();

	final TypeUtils reflector = ContextProvider.getApplicationContext().getBean(TypeUtils.class);

	void buildAfterStartUp() throws Exception;

}
