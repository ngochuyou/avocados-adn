package adn.service.transaction;

import java.util.Arrays;

import javax.transaction.xa.Xid;

public class GloballyManagedTransaction implements Transaction {

	private TransactionId id;

	private Event event;

	public GloballyManagedTransaction(String id) {
		this.id = new TransactionId(id);
	}

	@Override
	public Transaction registerEvent(Event e) {
		// TODO Auto-generated method stub
		if (this.event == null) {
			this.event = e;

			return this;
		}

		this.event.and(e);
		
		return this;
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public TransactionId getId() {
		// TODO Auto-generated method stub
		return this.id;
	}

}

class TransactionId implements Xid {

	byte[] gbTrId;

	public TransactionId(String idString) {
		// TODO Auto-generated constructor stub
		this.gbTrId = idString.getBytes();
	}

	@Override
	public int getFormatId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		// TODO Auto-generated method stub
		return this.gbTrId;
	}

	@Override
	public byte[] getBranchQualifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (other instanceof Xid) {
			Xid id = (Xid) other;

			return Arrays.equals(this.gbTrId, id.getGlobalTransactionId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Arrays.hashCode(this.gbTrId);
	}

}
