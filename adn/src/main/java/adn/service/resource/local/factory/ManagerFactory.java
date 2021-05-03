/**
 * 
 */
package adn.service.resource.local.factory;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.boot.internal.StandardEntityNotFoundDelegate;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.query.spi.NamedQueryRepository;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.spi.TypeConfiguration;
import org.springframework.util.Assert;

import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.local.SharedIdentifierGeneratorFactory;
import adn.service.resource.metamodel.EntityBinder;
import adn.service.resource.metamodel.MetamodelImpl;
import adn.service.resource.metamodel.MetamodelImplementor;
import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public class ManagerFactory implements EntityManagerFactoryImplementor {

	private static final long serialVersionUID = 1L;

	private final LocalResourceStorage localStorage;

	private final MetamodelImplementor metamodel;
	private final ContextBuildingService buildingService;

	private final SharedIdentifierGeneratorFactory sharedIdentifierGeneratorFactory;
	private final TypeConfiguration typeConfiguration;
	private final Metadata metadata;
	private final Dialect dialect;

	private final EntityNotFoundDelegate eNFD = new StandardEntityNotFoundDelegate();

	private final FastSessionServices fastSessionServices;

	public ManagerFactory(final ContextBuildingService serviceContext, final TypeConfiguration typeConfiguration) {
		// TODO Auto-generated constructor stub
		Assert.notNull(serviceContext, "ContextBuildingService cannot be null");

		Metadata metadata = serviceContext.getService(Metadata.class);

		Assert.notNull(metadata, "Metadata must not be null");

		this.metadata = metadata;
		this.typeConfiguration = typeConfiguration;
		buildingService = serviceContext;
		localStorage = serviceContext.getService(LocalResourceStorage.class);
		sharedIdentifierGeneratorFactory = new SharedIdentifierGeneratorFactory(serviceContext);
		EntityBinder.INSTANCE = new EntityBinder(sharedIdentifierGeneratorFactory);
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
	public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
		// TODO Auto-generated method stub
		return sharedIdentifierGeneratorFactory;
	}

	@Override
	public Type getIdentifierType(String className) throws MappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifierPropertyName(String className) throws MappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getReferencedPropertyType(String className, String propertyName) throws MappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionFactoryOptions getSessionFactoryOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session openSession() throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session getCurrentSession() throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatelessSessionBuilder<?> withStatelessOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatelessSession openStatelessSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatelessSession openStatelessSession(Connection connection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<?> getDefinedFilterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsFetchProfileDefinition(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TypeHelper getTypeHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassMetadata getClassMetadata(@SuppressWarnings("rawtypes") Class entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassMetadata getClassMetadata(String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionMetadata getCollectionMetadata(String roleName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ClassMetadata> getAllClassMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<?, ?> getAllCollectionMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager createEntityManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType,
			@SuppressWarnings("rawtypes") Map map) {
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
	public Map<String, Object> getProperties() {
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
	public Reference getReference() throws NamingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type resolveParameterBindType(Object bindValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type resolveParameterBindType(@SuppressWarnings("rawtypes") Class clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session openTemporarySession() throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CacheImplementor getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatisticsImplementor getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceRegistryImplementor getServiceRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Interceptor getInterceptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryPlanCache getQueryPlanCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamedQueryRepository getNamedQueryRepository() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FetchProfile getFetchProfile(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityNotFoundDelegate getEntityNotFoundDelegate() {
		// TODO Auto-generated method stub
		return eNFD;
	}

	@Override
	public SQLFunctionRegistry getSqlFunctionRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addObserver(SessionFactoryObserver observer) {
		// TODO Auto-generated method stub
	}

	@Override
	public CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FastSessionServices getFastSessionServices() {
		// TODO Auto-generated method stub
		return fastSessionServices;
	}

	@Override
	public DeserializationResolver<?> getDeserializationResolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JdbcServices getJdbcServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetamodelImplementor getMetamodel() {
		// TODO Auto-generated method stub
		return metamodel;
	}

	@Override
	public <T> List<RootGraphImplementor<? super T>> findEntityGraphsByJavaType(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RootGraphImplementor<?> findEntityGraphByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocalResourceStorage getStorage() {
		// TODO Auto-generated method stub
		return localStorage;
	}

	@Override
	public ContextBuildingService getContextBuildingService() {
		// TODO Auto-generated method stub
		return buildingService;
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

}
