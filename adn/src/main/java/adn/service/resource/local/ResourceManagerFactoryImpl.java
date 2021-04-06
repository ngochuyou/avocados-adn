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

import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.service.resource.metamodel.Metamodel;
import adn.service.resource.metamodel.MetamodelImpl;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceManagerFactoryImpl implements ResourceManagerFactory {

	private final NamingStrategy resourceNamingStrategy;

	private final LocalResourceStorage localStorage;

	private final Metamodel metamodel;

	private final ContextBuildingService buildingService;

	public ResourceManagerFactoryImpl(final ContextBuildingService serviceContext) {
		// TODO Auto-generated constructor stub
		Assert.notNull(serviceContext, "ContextBuildingService cannot be null");

		buildingService = serviceContext;
		resourceNamingStrategy = serviceContext.getService(NamingStrategy.class);
		localStorage = serviceContext.getService(LocalResourceStorage.class);

		Assert.notNull(localStorage, "LocalResourceStorage cannot be null");

		metamodel = new MetamodelImpl(serviceContext, this);
		metamodel.process();
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
	public <T> ResourceDescriptor<T> getResourceDescriptor(Class<T> resourceClass) {
		// TODO Auto-generated method stub
		return getResourceDescriptor(resourceNamingStrategy.getName(resourceClass));
	}

	@Override
	public <T> ResourceDescriptor<T> getResourceDescriptor(String resourceName) {
		// TODO Auto-generated method stub
		return metamodel.getResourceDescriptor(resourceName);
	}

	public <T> ResourceDescriptor<T> locateResourceDescriptor(Class<T> type) {
		supportCheck(type);

		return getResourceDescriptor(type);
	}

	private <T> void supportCheck(Class<T> type) throws IllegalArgumentException {
		ResourceDescriptor<T> descriptor = getResourceDescriptor(type);

		Assert.notNull(descriptor, "Unable to locate descriptor for resource of type: " + type
				+ ", provided resource type is not a managed type");
		Assert.isTrue(descriptor.isInstance(type), "Type check failed. Denied by: " + descriptor.getClass().getName());
	}

	@Override
	public LocalResourceStorage getStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		return metamodel;
	}

	@Override
	public ResourceManager createEntityManager() {
		// TODO Auto-generated method stub
		return ContextProvider.getApplicationContext().getBean(ResourceManager.class);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public EntityManager createEntityManager(Map map) {
		// TODO Auto-generated method stub
		return null;
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

}
