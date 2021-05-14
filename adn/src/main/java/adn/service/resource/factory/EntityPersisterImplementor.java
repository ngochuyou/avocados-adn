/**
 * 
 */
package adn.service.resource.factory;

import org.hibernate.persister.entity.EntityPersister;

import adn.service.resource.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityPersisterImplementor<T> extends ResourcePersister<T>, EntityPersister {

	@Override
	ResourcePersister<T> getEntityPersister();

	@Override
	EntityManagerFactoryImplementor getFactory();

}
