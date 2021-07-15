/**
 * 
 */
package adn.service;

/**
 * @author Ngoc Huy
 *
 */
public interface ObservableAccountService extends Observable {

	@Override
	default void register(Observer observer) {
		register((AccountServiceObserver) observer);
	}

	void register(AccountServiceObserver observer);

}
