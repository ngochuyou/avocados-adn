/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;

import org.hibernate.dialect.Dialect;
import org.hibernate.service.Service;
import org.hibernate.type.spi.TypeConfiguration;

import adn.service.resource.metamodel.MetamodelImplementor;
import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManagerFactory extends EntityManagerFactory {

	<T> ResourcePersister<T> getResourceDescriptor(String name);

	<T> ResourcePersister<T> getResourceDescriptor(Class<T> clazz);

	<T> ResourcePersister<T> locateResourceDescriptor(Class<T> clazz) throws IllegalArgumentException;

	boolean isLocked(Serializable identifier);

	boolean setLocked(Serializable identifier, boolean isLocked);

	LocalResourceStorage getStorage();

	ContextBuildingService getContextBuildingService();

	SharedIdentifierGeneratorFactory getIdentifierGeneratorFactory();

	TypeConfiguration getTypeConfiguration();

	Metadata getMetadata();

	@Override
	MetamodelImplementor getMetamodel();

	Dialect getDialect();

	public interface ServiceWrapper<T> extends Service {

		T unwrap();

	}

}
