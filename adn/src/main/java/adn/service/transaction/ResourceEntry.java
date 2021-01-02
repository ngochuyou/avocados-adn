/**
 * 
 */
package adn.service.transaction;

import java.io.Serializable;

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
	
	Serializable getId();
	
	Object getValue();
	
	ResourceKey getResourceKey();
	
	String getResourceName();
	
}
