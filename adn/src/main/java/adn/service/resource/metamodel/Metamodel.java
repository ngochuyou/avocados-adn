/**
 * 
 */
package adn.service.resource.metamodel;

import javax.persistence.PersistenceException;

import adn.service.resource.local.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public interface Metamodel extends javax.persistence.metamodel.Metamodel {

	<T> ResourcePersister<T> getResourceDescriptor(String name);

	void prepare() throws PersistenceException;

	void process() throws PersistenceException;

	void postProcess() throws PersistenceException;

}
