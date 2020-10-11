package adn.service.transaction;

import javax.transaction.xa.Xid;

/**
 * @author Ngoc Huy
 *
 */
public interface Transaction {
	
	Transaction registerEvent(Event e);
	
	void commit();
	
	void rollback();
	
	void reset();
	
	Xid getId();

}
