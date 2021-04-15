/**
 * 
 */
package adn.service.resource.metamodel;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;

import adn.service.resource.local.ResourcePersister;

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
	EntityTuplizerImplementor<T> getEntityTuplizer();
	
}
