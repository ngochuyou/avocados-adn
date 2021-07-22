/**
 * 
 */
package adn.service;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface ObservableDomainEntityService<T extends DomainEntity> extends Observable {

	@SuppressWarnings("unchecked")
	@Override
	default void register(Observer observer) {
		register((DomainEntityServiceObserver<T>) observer);
	}

	void register(DomainEntityServiceObserver<T> observer);

}
