/**
 * 
 */
package adn.service;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface DomainEntityServiceObserver<T extends DomainEntity> extends Observer {

	void notifyCreation(T newInstance);
	
	void notifyUpdate(T newState);

}
