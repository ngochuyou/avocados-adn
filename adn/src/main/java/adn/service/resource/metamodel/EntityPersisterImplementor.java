/**
 * 
 */
package adn.service.resource.metamodel;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;

import adn.service.resource.ResourcePersister;
import adn.service.resource.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityPersisterImplementor<T> extends EntityPersister {

	@Override
	ResourcePersister<T> getSubclassEntityPersister(Object instance, SessionFactoryImplementor factory);

	@Override
	ResourcePersister<T> getEntityPersister();

	@Override
	EntityManagerFactoryImplementor getFactory();

}
