/**
 * 
 */
package adn.service.resource.metamodel;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityPersisterImplementor<T> extends EntityPersister {

	@Override
	EntityPersisterImplementor<T> getSubclassEntityPersister(Object instance, SessionFactoryImplementor factory);

	@Override
	EntityPersisterImplementor<T> getEntityPersister();

	@Override
	EntityTuplizerImplementor<T> getEntityTuplizer();

}
