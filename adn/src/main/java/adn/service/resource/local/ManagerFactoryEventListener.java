/**
 * 
 */
package adn.service.resource.local;

import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
public interface ManagerFactoryEventListener {

	void postBuild(ResourceManagerFactory managerFactory);

	default void listen() {
		ContextProvider.getApplicationContext().getBean(ResourceManagerFactoryBuilder.class).addEventListener(this);
	}

}
