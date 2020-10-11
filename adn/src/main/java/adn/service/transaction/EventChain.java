package adn.service.transaction;

public class EventChain implements Event {

	private Action commitAction;
	
	private Action rollbackAction;
	
	private Event next;
	
	private Event prev;
	
	@Override
	public void commit() {
		// TODO Auto-generated method stub
		try {
			this.commitAction.execute();
			
			if (this.next != null) {
				this.next.commit();
			}
		} catch (Exception e) {
			try {
				this.rollback();
			} catch (TransactionException te) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void rollback() throws TransactionException {
		// TODO Auto-generated method stub
		try {
			this.rollbackAction.execute();
			this.prev.rollback();
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new TransactionException(e.getMessage());
		}
	}
	
	@Override
	public Event and(Event e) {
		// TODO Auto-generated method stub
		this.next = e;
		
		return this;
	}

}
