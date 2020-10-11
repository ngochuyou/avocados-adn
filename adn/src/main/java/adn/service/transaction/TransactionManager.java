package adn.service.transaction;

import javax.transaction.xa.Xid;

public interface TransactionManager {

	Transaction begin();
	
	Transaction getTransaction(Xid id);
	
	void end(Xid id);
	
	boolean contains(Xid id);
	
	void clear();

}
