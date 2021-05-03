/**
 * 
 */
package adn.service.resource.local;

import adn.application.context.ContextProvider;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
public interface ManagerFactoryEventListener {

	void postBuild(EntityManagerFactoryImplementor managerFactory);

	default void listen() {
		ContextProvider.getApplicationContext().getBean(ResourceManagerFactoryBuilder.class).addEventListener(this);
	}

}
