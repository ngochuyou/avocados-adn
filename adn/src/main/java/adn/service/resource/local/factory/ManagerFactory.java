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
	private final StatisticsImplementor nonStats = new NonStatistic();
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

		return sharedIdentifierGeneratorFactory;
	}

	@Override
	public Type getIdentifierType(String className) throws MappingException {

		return null;
	}

	@Override
	public String getIdentifierPropertyName(String className) throws MappingException {

		return null;
	}

	@Override
	public Type getReferencedPropertyType(String className, String propertyName) throws MappingException {

		return null;
	}

	@Override
	public SessionFactoryOptions getSessionFactoryOptions() {
		return null;
	}

	@Override
	public Session openSession() throws HibernateException {

		return null;
	}

	@Override
	public Session getCurrentSession() throws HibernateException {

		return null;
	}

	@Override
	public StatelessSessionBuilder<?> withStatelessOptions() {

		return null;
	}

	@Override
	public StatelessSession openStatelessSession() {

		return null;
	}

	@Override
	public StatelessSession openStatelessSession(Connection connection) {

		return null;
	}

	@Override
	public void close() throws HibernateException {

	}

	@Override
	public boolean isClosed() {

		return false;
	}

	@Override
	public Set<?> getDefinedFilterNames() {

		return null;
	}

	@Override
	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {

		return null;
	}

	@Override
	public boolean containsFetchProfileDefinition(String name) {

		return false;
	}

	@Override
	public TypeHelper getTypeHelper() {

		return null;
	}

	@Override
	public ClassMetadata getClassMetadata(@SuppressWarnings("rawtypes") Class entityClass) {

		return null;
	}

	@Override
	public ClassMetadata getClassMetadata(String entityName) {

		return null;
	}

	@Override
	public CollectionMetadata getCollectionMetadata(String roleName) {

		return null;
	}

	@Override
	public Map<String, ClassMetadata> getAllClassMetadata() {

		return null;
	}

	@Override
	public Map<?, ?> getAllCollectionMetadata() {

		return null;
	}

	@Override
	public EntityManager createEntityManager() {

		return null;
	}

	@Override
	public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {

		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType) {

		return null;
	}

	@Override
	public EntityManager createEntityManager(SynchronizationType synchronizationType,
			@SuppressWarnings("rawtypes") Map map) {

		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {

		return null;
	}

	@Override
	public boolean isOpen() {

		return false;
	}

	@Override
	public Map<String, Object> getProperties() {

		return null;
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {

		return null;
	}

	@Override
	public void addNamedQuery(String name, Query query) {

	}

	@Override
	public <T> T unwrap(Class<T> cls) {

		return null;
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {

	}

	@Override
	public Reference getReference() throws NamingException {

		return null;
	}

	@Override
	public Type resolveParameterBindType(Object bindValue) {

		return null;
	}

	@Override
	public Type resolveParameterBindType(@SuppressWarnings("rawtypes") Class clazz) {

		return null;
	}

	@Override
	public String getUuid() {

		return null;
	}

	@Override
	public String getName() {

		return null;
	}

	@Override
	public Session openTemporarySession() throws HibernateException {

		return null;
	}

	@Override
	public CacheImplementor getCache() {

		return null;
	}

	@Override
	public StatisticsImplementor getStatistics() {

		return nonStats;
	}

	@Override
	public ServiceRegistryImplementor getServiceRegistry() {

		return null;
	}

	@Override
	public Interceptor getInterceptor() {

		return null;
	}

	@Override
	public QueryPlanCache getQueryPlanCache() {

		return null;
	}

	@Override
	public NamedQueryRepository getNamedQueryRepository() {

		return null;
	}

	@Override
	public FetchProfile getFetchProfile(String name) {

		return null;
	}

	@Override
	public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {

		return null;
	}

	@Override
	public EntityNotFoundDelegate getEntityNotFoundDelegate() {

		return eNFD;
	}

	@Override
	public SQLFunctionRegistry getSqlFunctionRegistry() {

		return null;
	}

	@Override
	public void addObserver(SessionFactoryObserver observer) {

	}

	@Override
	public CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy() {

		return null;
	}

	@Override
	public CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver() {

		return null;
	}

	@Override
	public FastSessionServices getFastSessionServices() {

		return fastSessionServices;
	}

	@Override
	public DeserializationResolver<?> getDeserializationResolver() {

		return null;
	}

	@Override
	public JdbcServices getJdbcServices() {

		return null;
	}

	@Override
	public MetamodelImplementor getMetamodel() {

		return metamodel;
	}

	@Override
	public <T> List<RootGraphImplementor<? super T>> findEntityGraphsByJavaType(Class<T> entityClass) {

		return null;
	}

	@Override
	public RootGraphImplementor<?> findEntityGraphByName(String name) {

		return null;
	}

	@Override
	public LocalResourceStorage getStorage() {

		return localStorage;
	}

	@Override
	public ContextBuildingService getContextBuildingService() {

		return buildingService;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {

		return typeConfiguration;
	}

	@Override
	public Metadata getMetadata() {

		return metadata;
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

}
