/**
 * 
 */
package adn.service.context;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.springframework.util.Assert;

/**
 * An instance of this interface represents a resource in the
 * {@link ServiceManager} <i><Context</i><br>
 * 
 * @author Ngoc Huy
 *
 */
public abstract class Resource implements XAResource {

	private final String id;

	/**
	 * 
	 */
	public Resource(String id) {
		// TODO Auto-generated constructor stub
		Assert.notNull(id, "Resource id cannot be null");
		this.id = id;
	}

	public String getId() {
		return id;
	}

	abstract Class<?> getResourceType();

	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public void end(Xid xid, int flags) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public void forget(Xid xid) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTransactionTimeout() throws XAException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback(Xid xid) throws XAException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void start(Xid xid, int flags) throws XAException {
		// TODO Auto-generated method stub

	}

}
