/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactoryBuilder.unsupport;

import javax.persistence.EntityGraph;

import org.hibernate.engine.spi.SessionFactoryImplementor;

import adn.service.resource.local.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public interface MetamodelImplementor extends Metamodel, org.hibernate.metamodel.spi.MetamodelImplementor {

	@Override
	@Deprecated
	default <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		// TODO Auto-generated method stub
	}

	@Override
	<X> ResourceType<X> entity(Class<X> cls);

	@Override
	<X> ResourceType<X> entity(String entityName);

	@Override
	ResourcePersister<?> entityPersister(@SuppressWarnings("rawtypes") Class entityClass);

	@Override
	ResourcePersister<?> entityPersister(String entityName);

	@Override
	@Deprecated
	default SessionFactoryImplementor getSessionFactory() {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	ResourcePersister<?> locateEntityPersister(@SuppressWarnings("rawtypes") Class byClass);

	@Override
	ResourcePersister<?> locateEntityPersister(String byName);

}
