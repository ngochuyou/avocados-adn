/**
 * 
 */
package adn.service;

import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
public interface AccountServiceObserver extends Observer {

	void notifyAccountUpdate(Account newState);

}
