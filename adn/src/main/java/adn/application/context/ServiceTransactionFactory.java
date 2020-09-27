package adn.application.context;

import org.springframework.stereotype.Component;

import adn.service.builder.DefaultServiceTransaction;
import adn.service.builder.ServiceTransaction;

@Component
public class ServiceTransactionFactory {

	private ServiceTransaction transaction = new DefaultServiceTransaction();

	public ServiceTransaction getTransaction() {

		return transaction;
	}

}
