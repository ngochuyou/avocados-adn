/**
 * 
 */
package adn.service.resource.engine.action;

import adn.service.resource.engine.LocalStorage;
import adn.service.resource.engine.query.Query;

/**
 * @author Ngoc Huy
 *
 */
public interface SaveAction {

	void execute(Query query) throws RuntimeException;

	LocalStorage getStorage();

}
