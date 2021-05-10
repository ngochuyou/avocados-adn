/**
 * 
 */
package adn.service.resource.factory;

import org.hibernate.MappingException;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionBuilderImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;

import adn.service.resource.ResourcePersister;
import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("deprecation")
public interface EntityManagerFactoryImplementor extends SessionFactoryImplementor {

	@Override
	@Deprecated
	default SessionBuilderImplementor<?> withOptions() {
		return null;
	}

	@Override
	@Deprecated
	default TypeResolver getTypeResolver() {
		return null;
	}

	@Override
	@Deprecated
	default Settings getSettings() {
		return null;
	}

	LocalResourceStorage getStorage();

	TypeConfiguration getTypeConfiguration();

	@SuppressWarnings("unchecked")
	default <D> ResourcePersister<D> getEntityPersister(Class<D> entityClass) {
		return (ResourcePersister<D>) getMetamodel().entityPersister(entityClass);
	}

	@Override
	default EntityPersister getEntityPersister(String entityName) throws MappingException {
		return (ResourcePersister<?>) getMetamodel().entityPersister(entityName);
	}

}
