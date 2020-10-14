/**
 * 
 */
package adn.service.context.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class GlobalTransactionManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Map<String, GlobalTransaction> transactionMap;

	@Autowired
	public GlobalTransactionManager() {
		this.transactionMap = new HashMap<>();
	}

	public GlobalTransaction openTransaction() {
		String id = UUID.randomUUID().toString();
		GlobalTransaction newTransaction = new GlobalTransaction(id);
		
		logger.debug("Opening new transaction: " + id);
		this.transactionMap.put(id, newTransaction);

		return newTransaction;
	}

	public boolean closeTransaction(String id) {
		if (this.transactionMap.containsKey(id)) {
			logger.debug("Closing transaction: " + id);
			this.transactionMap.get(id).setLockMode(Transaction.LockMode.FATAL);
			this.transactionMap.remove(id);
			
			return true;
		}

		return false;
	}

	public GlobalTransaction getTransaction(String id) {
		// TODO Auto-generated method stub
		return this.transactionMap.get(id);
	}
	
	public void lockTransaction(String id) {
		if (this.transactionMap.containsKey(id)) {
			this.transactionMap.get(id).setLockMode(Transaction.LockMode.FATAL);
		}
	}

}
