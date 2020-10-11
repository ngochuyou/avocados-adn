package adn.application.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.transaction.xa.Xid;

import org.springframework.stereotype.Component;

import adn.service.transaction.GloballyManagedTransaction;
import adn.service.transaction.TransactionManager;

@Component
public class GlobalTransactionManager implements TransactionManager {

	private Map<Xid, GloballyManagedTransaction> transactionMap;

	public GlobalTransactionManager() {
		this.transactionMap = new HashMap<>();
	}

	@Override
	public GloballyManagedTransaction begin() {
		// TODO Auto-generated method stub
		GloballyManagedTransaction transaction = new GloballyManagedTransaction(UUID.randomUUID().toString());
		
		this.transactionMap.put(transaction.getId(), transaction);
		
		return transaction;
	}

	@Override
	public GloballyManagedTransaction getTransaction(Xid id) {
		// TODO Auto-generated method stub
		return this.transactionMap.get(id);
	}
	
	@Override
	public void end(Xid id) {
		// TODO Auto-generated method stub
		this.transactionMap.remove(id);
	}
	
	@Override
	public boolean contains(Xid id) {
		// TODO Auto-generated method stub
		return this.transactionMap.containsKey(id);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
