package adn.application.context;

import org.springframework.stereotype.Component;

import adn.service.builder.SynchornizedServiceTransaction;
import adn.service.builder.ServiceTransaction;

@Component
public class TransactionFactory {

	private ServiceTransaction transaction = new SynchornizedServiceTransaction();

	public ServiceTransaction getTransaction() {
		
		return transaction;
	}

}
