/**
 * 
 */
package adn.service.resource.metamodel;

import javax.persistence.PersistenceException;

import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.local.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public interface Metamodel extends org.hibernate.Metamodel {

	<T> ResourcePersister<T> getResourceDescriptor(String name);

	void prepare() throws PersistenceException;

	void process() throws PersistenceException;

	void postProcess() throws PersistenceException;

	ResourceManagerFactory getManagerFactory();

	<E extends Metamodel> E unwrap(Class<? super E> type);

}
