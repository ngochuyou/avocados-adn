/**
 * 
 */
package adn.service.resource.factory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.metamodel.internal.JpaMetaModelPopulationSetting;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.query.spi.NamedQueryRepository;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.spi.TypeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.service.resource.SharedIdentifierGeneratorFactory;
import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public class ManagerFactory implements EntityManagerFactoryImplementor {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final LocalResourceStorage localStorage;

	private final MetamodelImplementor metamodel;
	private final SessionFactoryOptions sessionFactoryOptions;
	private final ServiceRegistry serviceRegistry;

	private final CacheImplementor cacheImplementor;
	private final SharedIdentifierGeneratorFactory sharedIdentifierGeneratorFactory;
	private final TypeConfiguration typeConfiguration;
	private final JdbcServices jdbcServices;
	private final Dialect dialect;
	private final transient Map<String, Object> properties;

	private final EntityNotFoundDelegate eNFD = new StandardEntityNotFoundDelegate();
	private final StatisticsImplementor nonStats = new NonStatistic();
	private final FastSessionServices fastSessionServices;

	private final Set<SessionFactoryObserver> observers = new HashSet<>();

	@SuppressWarnings("unchecked")
	public ManagerFactory(
	// @formatter:off
			final LocalResourceStorage localStorage,
			final TypeConfiguration typeConfiguration,
			final MetadataImplementor metadata,
			final ServiceRegistry serviceRegistry,
			final SessionFactoryOptions sessionFactoryOptions,
			final FastSessionServices fsses) throws IllegalAccessException {
		// @formatter:on
		Assert.notNull(localStorage, String.format("[%s] must not be null", LocalResourceStorage.class));
		Assert.notNull(typeConfiguration, String.format("[%s] must not be null", TypeConfiguration.class));
		Assert.notNull(metadata, String.format("[%s] must not be null", MetadataImplementor.class));
		Assert.notNull(serviceRegistry, String.format("[%s] must not be null", ServiceRegistry.class));
		Assert.notNull(sessionFactoryOptions, String.format("[%s] must not be null", SessionFactoryOptions.class));
		Assert.notNull(fsses, String.format("[%s] must not be null", FastSessionServices.class));
		Assert.notNull(serviceRegistry.getService(MutableIdentifierGeneratorFactory.class),
				String.format("Unable to locate [%s] from [%s] instance",
						MutableIdentifierGeneratorFactory.class.getName(), ServiceRegistry.class));
		Assert.notNull(serviceRegistry.getService(JdbcServices.class), String.format(
				"Unable to locate [%s] from [%s] instance", JdbcServices.class.getName(), ServiceRegistry.class));

		for (SessionFactoryObserver sessionFactoryObserver : sessionFactoryOptions.getSessionFactoryObservers()) {
			observers.add(sessionFactoryObserver);
		}

		this.typeConfiguration = typeConfiguration;
		this.sessionFactoryOptions = sessionFactoryOptions;
		this.serviceRegistry = serviceRegistry;
		this.localStorage = localStorage;
		sharedIdentifierGeneratorFactory = new SharedIdentifierGeneratorFactory(
				typeConfiguration.getBasicTypeRegistry(),
				serviceRegistry.getService(MutableIdentifierGeneratorFactory.class));
		cacheImplementor = serviceRegistry.getService(CacheImplementor.class);
		jdbcServices = serviceRegistry.getService(JdbcServices.class);
		dialect = jdbcServices.getDialect();
		fastSessionServices = fsses;

		this.properties = new HashMap<>();
		this.properties.putAll(serviceRegistry.getService(ConfigurationService.class).getSettings());

		if (!properties.containsKey(AvailableSettings.JPA_VALIDATION_FACTORY)) {
			if (getSessionFactoryOptions().getValidatorFactoryReference() != null) {
				properties.put(AvailableSettings.JPA_VALIDATION_FACTORY,
						getSessionFactoryOptions().getValidatorFactoryReference());
			}
		}

		this.metamodel = typeConfiguration.scope(this);

		((MetamodelImpl) metamodel).initialize(metadata, JpaMetaModelPopulationSetting.IGNORE_UNSUPPORTED);

		ContextProvider.getAccess().setLocalResourceSessionFactory(this);
		observers.forEach(observer -> observer.sessionFactoryCreated(this));
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
		return sessionFactoryOptions;
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
		return properties;
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
		return cacheImplementor;
	}

	@Override
	public StatisticsImplementor getStatistics() {
		return nonStats;
	}

	@Override
	public ServiceRegistryImplementor getServiceRegistry() {
		return (ServiceRegistryImplementor) serviceRegistry;
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
		if (observers.contains(observer)) {
			return;
		}

		logger.trace(String.format("Added a new observer -> [%s]", observer.getClass()));
		observers.add(observer);
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
		return jdbcServices;
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
	public TypeConfiguration getTypeConfiguration() {

		return typeConfiguration;
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

}
