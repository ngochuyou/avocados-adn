package adn.service.transaction;

public interface Event {

	void commit();

	void rollback() throws TransactionException;

	Event and(Event e);
	
}
