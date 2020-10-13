/**
 * 
 */
package adn.service.transaction;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ngoc Huy
 *
 */
public class GlobalTransaction implements Transaction {

	private String id;

	private Set<Event<?>> actions;

	private Set<Event<?>> rollbacks;

	private LockMode lockMode;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected GlobalTransaction(String id) {
		super();
		this.id = id;
		this.actions = new HashSet<>();
		this.rollbacks = new HashSet<>();
		this.lockMode = LockMode.NONE;
	}

	@Override
	public void commit() throws TransactionException {
		// TODO Auto-generated method stub
		this.lockMode = LockMode.TEMP;

		for (Event<?> ev : actions) {
			try {
				ev.execute();
			} catch (Exception e) {
				e.printStackTrace();

				this.lockMode = LockMode.FATAL;
				logger.error(e.getMessage());
				throw new TransactionException(e.getMessage(), id);
			}
		}
		
		this.lockMode = LockMode.NONE;
	}

	@Override
	public void rollback() throws TransactionException {
		// TODO Auto-generated method stub
		for (Event<?> ev : rollbacks) {
			try {
				ev.execute();
			} catch (Exception e) {
				this.lockMode = LockMode.FATAL;
				logger.error("Cannot execute rollback: " + e.getMessage());
				throw new TransactionException(e.getMessage(), id);
			}
		}
	}

	@Override
	public <T> void addAction(Event<T> action) {
		// TODO Auto-generated method stub
		this.actions.add(action);
	}

	@Override
	public <T> void addRollback(Event<T> rollback) {
		// TODO Auto-generated method stub
		this.rollbacks.add(rollback);
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	public void lock() {
		this.lockMode = LockMode.TEMP;
	}

	public void unlock() {
		this.lockMode = LockMode.NONE;
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	protected void setLockMode(LockMode lockMode) {
		this.lockMode = lockMode;
	}

}
