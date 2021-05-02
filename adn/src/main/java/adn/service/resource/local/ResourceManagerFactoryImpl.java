/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.boot.internal.StandardEntityNotFoundDelegate;
import org.hibernate.dialect.Dialect;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.type.spi.TypeConfiguration;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.service.resource.metamodel.Metamodel;
import adn.service.resource.metamodel.MetamodelImpl;
import adn.service.resource.metamodel.MetamodelImplementor;
import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceManagerFactoryImpl implements ResourceManagerFactory {

	private final LocalResourceStorage localStorage;

	private final Metamodel metamodel;
	private final ContextBuildingService buildingService;

	private final SharedIdentifierGeneratorFactory sharedIdentifierGeneratorFactory;
	private final TypeConfiguration typeConfiguration;
	private final Metadata metadata;
	private final Dialect dialect;

	private final LoadEventListener loadEventListener = new DefaultLoadEventListenerImplementor();
	private final EntityNotFoundDelegate eNFD = new StandardEntityNotFoundDelegate();

	private final FastSessionServices fastSessionServices;

	public ResourceManagerFactoryImpl(final ContextBuildingService serviceContext,
			final TypeConfiguration typeConfiguration) {
		// TODO Auto-generated constructor stub
		Assert.notNull(serviceContext, "ContextBuildingService cannot be null");

		Metadata metadata = serviceContext.getService(Metadata.class);

		Assert.notNull(metadata, "Metadata must not be null");

		this.metadata = metadata;
		this.typeConfiguration = typeConfiguration;
		buildingService = serviceContext;
		localStorage = serviceContext.getService(LocalResourceStorage.class);
		sharedIdentifierGeneratorFactory = new SharedIdentifierGeneratorFactory(serviceContext);
		dialect = serviceContext.getServiceWrapper(Dialect.class, wrapper -> wrapper.orElseThrow().unwrap());

		Assert.notNull(localStorage, "LocalResourceStorage cannot be null");

		FastSessionServices fsses = serviceContext.getServiceWrapper(FastSessionServices.class,
				wrapper -> wrapper.orElseThrow().unwrap());

		Assert.notNull(fsses, "Unable to find instance of " + FastSessionServices.class);

		fastSessionServices = fsses;

		metamodel = new MetamodelImpl(serviceContext, this);
		metamodel.prepare();
		metamodel.process();
		metamodel.postProcess();
	}

	@Override
	public boolean isLocked(Serializable identifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setLocked(Serializable identifier, boolean isLocked) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LocalResourceStorage getStorage() {
		// TODO Auto-generated method stub
		return localStorage;
	}

	@Override
	public MetamodelImplementor getMetamodel() {
		return metamodel.unwrap(MetamodelImplementor.class);
	}

	@Override
	public EntityManager createEntityManager() {
		// TODO Auto-generated method stub
		return ((ResourceManager) ContextProvider.getApplicationContext().getBean(ResourceManager.class.getName()))
				.unwrapManager(EntityManager.class);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public EntityManager createEntityManager(Map map) {
		// TODO Auto-generated method stub
		return createEntityManager();
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cache getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNamedQuery(String name, Query query) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		// TODO Auto-generated method stub

	}

	@Override
	public ContextBuildingService getContextBuildingService() {
		// TODO Auto-generated method stub
		return buildingService;
	}

	@Override
	public SharedIdentifierGeneratorFactory getIdentifierGeneratorFactory() {
		// TODO Auto-generated method stub
		return sharedIdentifierGeneratorFactory;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		// TODO Auto-generated method stub
		return typeConfiguration;
	}

	@Override
	public Metadata getMetadata() {
		// TODO Auto-generated method stub
		return metadata;
	}

	@Override
	public Dialect getDialect() {
		// TODO Auto-generated method stub
		return dialect;
	}

	@Override
	public LoadEventListener getLoadEventListener() {
		// TODO Auto-generated method stub
		return loadEventListener;
	}

	@Override
	public EntityNotFoundDelegate getResourceNotFoundHandler() {
		// TODO Auto-generated method stub
		return eNFD;
	}

	@Override
	public FastSessionServices getFastSessionServices() {
		// TODO Auto-generated method stub
		return fastSessionServices;
	}

}
