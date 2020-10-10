package adn.service.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchornizedServiceTransaction implements ServiceTransaction {

	private List<Event> transactionalEvents = new ArrayList<>();

	private List<Event> eventRollbacks = new ArrayList<>();

	private TransactionStrategy strategy = TransactionStrategy.NON_TRANSACTION;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public synchronized boolean commit() {
		// TODO Auto-generated method stub
		if (this.strategy.equals(TransactionStrategy.NON_TRANSACTION)) {
			this.clear();

			return true;
		}

		for (Event event : this.transactionalEvents) {
			try {
				event.method.setAccessible(true);
				event.method.invoke(event.invoker, event.values);
				logger.debug("Invoking commit method: " + event.method.getName());
			} catch (InvocationTargetException ite) {
				ite.getTargetException().printStackTrace();

				return false;
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();

				return false;
			} finally {
				this.clear();
			}
		}

		this.clear();

		return true;
	}

	@Override
	public synchronized boolean rollback() {
		// TODO Auto-generated method stub
		for (Event event : this.eventRollbacks) {
			try {
				event.method.setAccessible(true);
				event.method.invoke(event.invoker, event.values);
				logger.debug("Invoking rollback method: " + event.method.getName());
			} catch (InvocationTargetException ite) {
				ite.getTargetException().printStackTrace();

				return false;
			} catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace();

				return false;
			} finally {
				this.clear();
			}
		}

		this.clear();

		return true;
	}

	@Override
	public synchronized boolean clear() {
		// TODO Auto-generated method stub
		this.transactionalEvents = new ArrayList<>();
		this.eventRollbacks = new ArrayList<>();
		
		return true;
	}

	@Override
	public synchronized Event registerEvent(Object invoker, Method method, Object[] values) {
		// TODO Auto-generated method stub
		Event event = new Event(invoker, method, values, this);

		this.transactionalEvents.add(event);

		return event;
	}

	@Override
	public synchronized Event registerRollback(Object invoker, Method method, Object[] values) {
		// TODO Auto-generated method stub
		Event event = new Event(invoker, method, values, this);

		this.eventRollbacks.add(event);

		return event;
	}

	@Override
	public synchronized void setStrategy(TransactionStrategy strategy) {
		// TODO Auto-generated method stub
		this.strategy = strategy;
	}

	@Override
	public synchronized TransactionStrategy getStrategy() {

		return strategy;
	}

}
