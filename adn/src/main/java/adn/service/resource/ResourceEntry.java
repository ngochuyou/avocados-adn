/**
 * 
 */
package adn.service.resource;

import java.io.Serializable;

import javax.persistence.LockModeType;

import adn.service.resource.persister.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceEntry {

	LockModeType getLockMode();

	void setLockMode(LockModeType type);

	Status getStatus();

	void setStatus(Status status);

	Serializable getId();

	Object[] getLoadedState();

	Object getLoadedValue(String propertyName);

	Object[] getDeletedState();

	void setDeletedState(Object[] deletedState);

	boolean isExistsStorage();

	Object getVersion();

	ResourcePersister getPersister();

	ResourceKey getResourceKey();

	String getResourceName();

	boolean isBeingReplicated();

	Object getRowId();

	void postUpdate(Object entity, Object[] updatedState, Object nextVersion);

	void postDelete();

	void postInsert(Object[] insertedState);

	boolean requiresDirtyCheck(Object entity);

	boolean isModifiableResource();

	void forceLocked(Object entity, Object nextVersion);

	boolean isReadOnly();

	void setReadOnly(boolean readOnly, Object entity);

}
