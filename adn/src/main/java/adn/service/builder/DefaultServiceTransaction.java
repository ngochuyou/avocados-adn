package adn.service.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultServiceTransaction implements ServiceTransaction {

	private List<TransactionalEvent> transactionalEvents = new ArrayList<>();

	private TransactionStrategy strategy = TransactionStrategy.NON_TRANSACTION;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public synchronized boolean commit() {
		// TODO Auto-generated method stub
		if (this.strategy.equals(TransactionStrategy.NON_TRANSACTION)) {
			return true;
		}
		
		for (TransactionalEvent event : this.transactionalEvents) {
			try {
				event.method.setAccessible(true);
				event.method.invoke(event.invoker, event.values);
				logger.debug("Invoking method: " + event.method.getName());
			} catch (InvocationTargetException ite) {
				ite.getTargetException().printStackTrace();
				
				return false;
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();

				return false;
			}
		}

		return true;
	}

	@Override
	public synchronized boolean rollback() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public synchronized boolean clear() {
		// TODO Auto-generated method stub
		this.transactionalEvents = new ArrayList<>();

		return true;
	}

	@Override
	public synchronized void registerEvent(TransactionalEvent event) {
		// TODO Auto-generated method stub
		this.transactionalEvents.add(event);
	}

	@Override
	public synchronized void setStrategy(TransactionStrategy strategy) {
		// TODO Auto-generated method stub
		this.strategy = strategy;
	}

	@Override
	public TransactionStrategy getStrategy() {

		return strategy;
	}

}
