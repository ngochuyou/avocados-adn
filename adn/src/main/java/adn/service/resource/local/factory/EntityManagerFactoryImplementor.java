/**
 * 
 */
package adn.service.resource.local.factory;

import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionBuilderImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.Service;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;

import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.metamodel.MetamodelImplementor;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	default TypeResolver getTypeResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	default Settings getSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	LocalResourceStorage getStorage();

	ContextBuildingService getContextBuildingService();

	TypeConfiguration getTypeConfiguration();

	Metadata getMetadata();

	Dialect getDialect();

	@Override
	MetamodelImplementor getMetamodel();

	public interface ServiceWrapper<T> extends Service {

		T unwrap();

	}

}
