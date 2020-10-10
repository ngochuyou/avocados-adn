package adn.service.builder;

import java.lang.reflect.Method;

public interface ServiceTransaction {
	
	enum TransactionStrategy {
		NON_TRANSACTION, TRANSACTIONAL
	}

	boolean commit();

	boolean rollback();

	boolean clear();

	void setStrategy(TransactionStrategy strategy);
	
	Event registerEvent(Object invoker, Method method, Object[] values);

	Event registerRollback(Object invoker, Method method, Object[] values);
	
	TransactionStrategy getStrategy();
	
}
