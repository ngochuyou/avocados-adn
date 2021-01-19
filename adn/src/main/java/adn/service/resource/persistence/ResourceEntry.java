/**
 * 
 */
package adn.service.resource.persistence;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

import javax.persistence.LockModeType;

import org.hibernate.engine.spi.Status;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceEntry {

	Serializable getId();
	
	LockModeType getLockMode();

	void setLockMode(LockModeType lockMode);

	Status getStatus();

	void setStatus(Status status);

	Object getVersion();

	ResourceKey getKey();

	Object getState();

	Object getPropertyValue();

	Type getResourceType();

	void postUpdate(Object resourceInstance, Object updatedState, Object nextVersion);

	void postDelete();

	void postInsert(Object insertedState);

	ResourcePersistenceContext getPersistenceContext();
	
	void serialize(ObjectOutputStream oos) throws IOException;
	
}
