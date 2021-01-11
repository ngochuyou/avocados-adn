/**
 * 
 */
package adn.service.resource.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.persistence.LockModeType;

import org.hibernate.engine.spi.Status;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResourceEntryImpl implements Serializable, ResourceEntry {

	private final Serializable id;

	private final Type type;

	private LockModeType lockMode;

	private Status status;

	private Object state;

	private Object version;

	public ResourceEntryImpl(Serializable id, LockModeType lockMode, Status status, Type type) {
		super();
		this.id = id;
		this.lockMode = lockMode;
		this.status = status;
		this.type = type;
	}

	@Override
	public LockModeType getLockMode() {
		// TODO Auto-generated method stub
		return lockMode;
	}

	@Override
	public void setLockMode(LockModeType lockMode) {
		// TODO Auto-generated method stub
		Assert.notNull(lockMode, "Entry lock mode can nit be null");
		this.lockMode = lockMode;
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return status;
	}

	@Override
	public void setStatus(Status status) {
		// TODO Auto-generated method stub
		Assert.notNull(status, "Entry status can nit be null");
		this.status = status;
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public Object getState() {
		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public Type getResourceType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Object getVersion() {
		// TODO Auto-generated method stub
		return version;
	}

	@Override
	public Object getPropertyValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postUpdate(Object resourceInstance, Object updatedState, Object nextVersion) {
		// TODO Auto-generated method stub
	}

	@Override
	public void postDelete() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postInsert(Object insertedState) {
		// TODO Auto-generated method stub
	}

}
