/**
 * 
 */
package adn.service.transaction;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.persistence.LockModeType;

import org.hibernate.engine.spi.Status;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceEntry {

	LockModeType getLockMode();

	void setLockMode(LockModeType lockMode);

	Status getStatus();

	void setStatus(Status status);

	Object getVersion();

	Serializable getId();

	Object getState();

	Object getPropertyValue();

	ResourceKey getResourceKey();

	Type getResourceType();

	void postUpdate(Object resourceInstance, Object updatedState, Object nextVersion);

	void postDelete();

	void postInsert(Object[] insertedState);

}
