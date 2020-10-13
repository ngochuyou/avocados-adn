/**
 * 
 */
package adn.service.transaction;

/**
 * @author Ngoc Huy
 *
 */
public interface Transaction {

	enum LockMode {
		NONE, TEMP, FATAL
	}

	void commit() throws TransactionException;

	void rollback() throws TransactionException;

	<T> void addAction(Event<T> action);

	<T> void addRollback(Event<T> rollback);

	LockMode getLockMode();

}
