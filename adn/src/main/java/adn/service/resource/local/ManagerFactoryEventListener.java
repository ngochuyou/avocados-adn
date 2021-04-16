/**
 * 
 */
package adn.service.resource.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ngoc Huy
 *
 */
public interface ManagerFactoryEventListener {

	final Logger logger = LoggerFactory.getLogger(ManagerFactoryEventListener.class);

	void postBuild(ResourceManagerFactory managerFactory);

}
