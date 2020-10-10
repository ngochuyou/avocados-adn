package adn.service.builder;

import java.lang.reflect.Method;

import adn.application.context.ContextProvider;
import adn.application.context.TransactionFactory;

public interface TransactionalService {

	final TransactionFactory transactionFactory = ContextProvider.getApplicationContext()
			.getBean(TransactionFactory.class);

	default Event registerEvent(Object invoker, Method method, Object[] values) {
		ServiceTransaction transaction = transactionFactory.getTransaction();

		return transaction.registerEvent(invoker, method, values);
	}

}
