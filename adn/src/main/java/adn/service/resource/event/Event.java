/**
 * 
 */
package adn.service.resource.event;

import java.io.Serializable;

import adn.service.resource.local.ResourceManager;

/**
 * @author Ngoc Huy
 *
 */
public interface Event extends Serializable {

	ResourceManager getResourceManager();

}
