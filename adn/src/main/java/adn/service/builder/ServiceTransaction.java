package adn.service.builder;

public interface ServiceTransaction {

	boolean commit();

	boolean rollback();

	boolean clear();

	void registerEvent(TransactionalEvent event);

	void setStrategy(TransactionStrategy strategy);

	enum TransactionStrategy {
		NON_TRANSACTION, TRANSACTIONAL
	}

	TransactionStrategy getStrategy();

}
