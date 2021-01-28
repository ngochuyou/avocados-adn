/**
 * 
 */
package adn.application.context;

import org.springframework.context.ApplicationContext;

import adn.utilities.GeneralUtilities;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuilder {

	final ApplicationContext context = ContextProvider.getApplicationContext();

	final GeneralUtilities reflector = ContextProvider.getApplicationContext().getBean(GeneralUtilities.class);

	void buildAfterStartUp() throws Exception;

}
