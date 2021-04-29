/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Selection;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.Interceptor;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MultiIdentifierLoadAccess;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.ReplicationMode;
import org.hibernate.ScrollMode;
import org.hibernate.SessionEventListener;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.cache.spi.CacheTransactionSynchronization;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.ExceptionConverter;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.graph.RootGraph;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.spi.NativeQueryImplementor;
import org.hibernate.query.spi.QueryImplementor;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.resource.jdbc.spi.JdbcSessionContext;
import org.hibernate.resource.transaction.spi.TransactionCoordinator;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import adn.service.resource.metamodel.Metamodel;

/**
 * @author Ngoc Huy
 *
 */
@Component
@RequestScope
@Lazy
@SuppressWarnings({ "serial", "rawtypes", "deprecation", "unchecked" })
public class LocalResourceSession implements SessionImplementor, ResourceManager, EventSource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final PersistenceContext resourceContext;

	private final ResourceManagerFactory resourceManagerFactory;

	private volatile boolean isRollbackOnly = false;

	/**
	 * 
	 */
	@Autowired
	public LocalResourceSession(
	// @formatter:off
			@NonNull final ResourceManagerFactory resourceManagerFactory) {
	// @formatter:on
		// TODO Auto-generated constructor stub
		this.resourceManagerFactory = resourceManagerFactory;
		resourceContext = createResourceContext();
	}

	private PersistenceContext createResourceContext() {
		return new ResourceContextImpl(this);
	}

	@Override
	public ResourceManagerFactory getResourceManagerFactory() {
		// TODO Auto-generated method stub
		return resourceManagerFactory;
	}

//	@Override
//	public <T> void manage(T instance, Class<T> type) {
//		// TODO Auto-generated method stub
//		eventFactory.createManageEvent(instance, type, this);
//	}
//
//	@Override
//	public <T> T find(Serializable identifier, Class<T> type) {
//		// TODO Auto-generated method stub
//		Object candidate = resourceContext.contains(identifier)
//				? resourceContext
//						.find(new ResourceKey<>(identifier, getResourceManagerFactory().locateResourceDescriptor(type)))
//				: null;
//
//		return TypeHelper.unwrap(candidate, type);
//	}
//
//	@Override
//	public <T> Serializable save(T instance) {
//		// TODO Auto-generated method stub
//		Class<T> type = TypeHelper.unwrapType(instance.getClass());
//
////		eventFactory.createSaveEvent(instance, type, this).fire();
//
//		return resourceManagerFactory.locateResourceDescriptor(type).getIdentifier(instance);
//	}

	@Override
	public void setRollbackOnly() {
		// TODO Auto-generated method stub
		this.isRollbackOnly = true;
	}

	@Override
	public boolean isRollbackOnly() {
		// TODO Auto-generated method stub
		return isRollbackOnly;
	}

	@Override
	public void persist(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public FlushModeType getFlushMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void detach(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LockModeType getLockMode(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(String propertyName, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void joinTransaction() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isJoinedToTransaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getDelegate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionFactoryImplementor getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionEventListenerManager getEventListenerManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JdbcCoordinator getJdbcCoordinator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JdbcServices getJdbcServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTenantIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getSessionIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkOpen(boolean markForRollbackIfClosed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markForRollbackOnly() {
		// TODO Auto-generated method stub

	}

	@Override
	public long getTransactionStartTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CacheTransactionSynchronization getCacheTransactionSynchronization() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTransactionInProgress() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Transaction accessTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityKey generateEntityKey(Serializable id, EntityPersister persister) {
		// TODO Auto-generated method stub
		return new EntityKey(id, persister);
	}

	@Override
	public Interceptor getInterceptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAutoClear(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeCollection(PersistentCollection collection, boolean writing) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object internalLoad(String entityName, Serializable id, boolean eager, boolean nullable)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object immediateLoad(String entityName, Serializable id) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List list(String query, QueryParameters queryParameters) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator iterate(String query, QueryParameters queryParameters) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScrollableResultsImplementor scroll(String query, QueryParameters queryParameters)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScrollableResultsImplementor scroll(Criteria criteria, ScrollMode scrollMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List list(Criteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List listFilter(Object collection, String filter, QueryParameters queryParameters)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator iterateFilter(Object collection, String filter, QueryParameters queryParameters)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityPersister getEntityPersister(String entityName, Object object) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getEntityUsingInterceptor(EntityKey key) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable getContextEntityIdentifier(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String bestGuessEntityName(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String guessEntityName(Object entity) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object instantiate(String entityName, Serializable id) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List listCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List list(NativeSQLQuerySpecification spec, QueryParameters queryParameters) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDontFlushFromFind() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeUpdate(String query, QueryParameters queryParameters) throws HibernateException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeNativeUpdate(NativeSQLQuerySpecification specification, QueryParameters queryParameters)
			throws HibernateException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CacheMode getCacheMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlushMode getHibernateFlushMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Connection connection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEventSource() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void afterScrollOperation() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> QueryImplementor<T> createQuery(String arg0, Class<T> arg1, Selection arg2, QueryOptions arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScrollableResultsImplementor scrollCustomQuery(CustomQuery customQuery, QueryParameters queryParameters)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScrollableResultsImplementor scroll(NativeSQLQuerySpecification spec, QueryParameters queryParameters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCacheMode(CacheMode cm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFlushMode(FlushMode flushMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHibernateFlushMode(FlushMode flushMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean shouldAutoClose() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAutoCloseSessionEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LoadQueryInfluencers getLoadQueryInfluencers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExceptionConverter getExceptionConverter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceContext getPersistenceContextInternal() {
		// TODO Auto-generated method stub
		return resourceContext;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Transaction beginTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction getTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcedureCall getNamedProcedureCall(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(Class persistentClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(Class persistentClass, String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Criteria createCriteria(String entityName, String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getJdbcBatchSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJdbcBatchSize(Integer jdbcBatchSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public JdbcSessionContext getJdbcSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JdbcConnectionAccess getJdbcConnectionAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionCoordinator getTransactionCoordinator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startTransactionBoundary() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTransactionBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTransactionCompletion() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTransactionCompletion(boolean successful, boolean delayed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void flushBeforeTransactionCompletion() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean shouldAutoJoinTransaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T execute(Callback<T> callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean useStreamForLobBinding() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LobCreator getLobCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeZone getJdbcTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryImplementor getNamedQuery(String queryName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryImplementor createQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> QueryImplementor<R> createQuery(String queryString, Class<R> resultClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> QueryImplementor<R> createNamedQuery(String name, Class<R> resultClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString, Class resultClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString, String resultSetMapping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NativeQueryImplementor getNamedNativeQuery(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		// TODO Auto-generated method stub
		return getResourceManagerFactory().getMetamodel();
	}

	@Override
	public PersistenceContext getPersistenceContext() {
		// TODO Auto-generated method stub
		return resourceContext;
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SharedSessionBuilder sessionWithOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancelQuery() throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirty() throws HibernateException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefaultReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDefaultReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub

	}

	@Override
	public Serializable getIdentifier(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(String entityName, Object object) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void evict(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T load(Class<T> theClass, Serializable id, LockMode lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T load(Class<T> theClass, Serializable id, LockOptions lockOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object load(String entityName, Serializable id, LockMode lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object load(String entityName, Serializable id, LockOptions lockOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T load(Class<T> theClass, Serializable id) {
		// TODO Auto-generated method stub
		return byId(theClass).getReference(id);
	}

	@Override
	public Object load(String entityName, Serializable id) {
		// TODO Auto-generated method stub
		return byId(entityName).getReference(id);
	}

	@Override
	public void load(Object object, Serializable id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void replicate(Object object, ReplicationMode replicationMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public Serializable save(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable save(String entityName, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveOrUpdate(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveOrUpdate(String entityName, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(String entityName, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object merge(String entityName, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persist(String entityName, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String entityName, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lock(Object object, LockMode lockMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lock(String entityName, Object object, LockMode lockMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public LockRequest buildLockRequest(LockOptions lockOptions) {
		// TODO Auto-generated method stub
		return new LockRequestImpl(lockOptions);
	}

	@Override
	public void refresh(String entityName, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object object, LockMode lockMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object object, LockOptions lockOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(String entityName, Object object, LockOptions lockOptions) {
		// TODO Auto-generated method stub

	}

	@Override
	public LockMode getCurrentLockMode(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public org.hibernate.Query createFilter(Object collection, String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Class<T> entityType, Serializable id) {
		// TODO Auto-generated method stub
		return byId(entityType).load(id);
	}

	@Override
	public <T> T get(Class<T> entityType, Serializable id, LockMode lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Class<T> entityType, Serializable id, LockOptions lockOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String entityName, Serializable id) {
		// TODO Auto-generated method stub
		return byId(entityName).load(id);
	}

	@Override
	public Object get(String entityName, Serializable id, LockMode lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String entityName, Serializable id, LockOptions lockOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEntityName(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdentifierLoadAccess<?> byId(String entityName) {
		// TODO Auto-generated method stub
		return new IdentifierLoadAccessImpl<>(locatePersister(entityName));
	}

	@Override
	public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultiIdentifierLoadAccess byMultipleIds(String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IdentifierLoadAccess<T> byId(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return new IdentifierLoadAccessImpl<>(locatePersister(entityClass));
	}

	@Override
	public NaturalIdLoadAccess byNaturalId(String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> NaturalIdLoadAccess<T> byNaturalId(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> SimpleNaturalIdLoadAccess<T> bySimpleNaturalId(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Filter enableFilter(String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Filter getEnabledFilter(String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disableFilter(String filterName) {
		// TODO Auto-generated method stub

	}

	@Override
	public SessionStatistics getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly(Object entityOrProxy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadOnly(Object entityOrProxy, boolean readOnly) {
		// TODO Auto-generated method stub

	}

	@Override
	public Connection disconnect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reconnect(Connection connection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enableFetchProfile(String name) throws UnknownProfileException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableFetchProfile(String name) throws UnknownProfileException {
		// TODO Auto-generated method stub

	}

	@Override
	public TypeHelper getTypeHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LobHelper getLobHelper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEventListeners(SessionEventListener... listeners) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object merge(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionImplementor getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockOptions getLockRequest(LockModeType lockModeType, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> RootGraphImplementor<T> createEntityGraph(Class<T> rootType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RootGraphImplementor<?> createEntityGraph(String graphName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RootGraphImplementor<?> getEntityGraph(String graphName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFlushBeforeCompletionEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ActionQueue getActionQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object instantiate(EntityPersister persister, Serializable id) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forceFlush(EntityEntry e) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public QueryImplementor createNamedQuery(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NativeQueryImplementor createSQLQuery(String sqlString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NativeQueryImplementor getNamedSQLQuery(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> QueryImplementor<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryImplementor createQuery(CriteriaUpdate updateQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryImplementor createQuery(CriteriaDelete deleteQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void merge(String entityName, Object object, Map copiedAlready) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void persist(String entityName, Object object, Map createdAlready) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void persistOnFlush(String entityName, Object object, Map copiedAlready) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(String entityName, Object object, Map refreshedAlready) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String entityName, Object child, boolean isCascadeDeleteEnabled, Set transientEntities) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeOrphanBeforeUpdates(String entityName, Object child) {
		// TODO Auto-generated method stub
	}

	public void afterOperation(boolean success) {
		logger.debug("Operation status: " + success);
//		if (!isTransactionInProgress()) {
//			getJdbcCoordinator().afterTransaction();
//		}
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return (T) this;
	}

	private class IdentifierLoadAccessImpl<T> implements IdentifierLoadAccess<T> {

		private final ResourcePersister<T> resourcePersister;

		private LockOptions lockOptions;

		IdentifierLoadAccessImpl(ResourcePersister<T> resourcePersister) {
			// TODO Auto-generated constructor stub
			this.resourcePersister = resourcePersister;
		}

		@Override
		public IdentifierLoadAccess<T> with(LockOptions lockOptions) {
			// TODO Auto-generated method stub
			this.lockOptions = lockOptions;
			return this;
		}

		@Override
		@Deprecated
		public IdentifierLoadAccess<T> with(CacheMode cacheMode) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		@Deprecated
		public IdentifierLoadAccess<T> with(RootGraph<T> graph, GraphSemantic semantic) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public T getReference(Serializable id) {
			// TODO Auto-generated method stub
			if (this.lockOptions != null) {
				LoadEvent event = new LoadEvent(id, resourcePersister.getEntityName(), lockOptions,
						LocalResourceSession.this, false);

				getResourceManagerFactory().getLoadEventListener().onLoad(event, LoadEventListener.IMMEDIATE_LOAD);

				return (T) event.getResult();
			}

			boolean success = false;

			try {
				LoadEvent event = new LoadEvent(id, resourcePersister.getEntityName(), false, LocalResourceSession.this,
						false);

				getResourceManagerFactory().getLoadEventListener().onLoad(event, LoadEventListener.IMMEDIATE_LOAD);

				if (event.getResult() == null) {
					getResourceManagerFactory().getResourceNotFoundHandler()
							.handleEntityNotFound(resourcePersister.getEntityName(), id);
				}

				success = true;

				return (T) event.getResult();
			} finally {
				afterOperation(success);
			}
		}

		@Override
		public T load(Serializable id) {
			// TODO Auto-generated method stub
			if (this.lockOptions != null) {
				LoadEvent event = new LoadEvent(id, resourcePersister.getEntityName(), lockOptions,
						LocalResourceSession.this, false);

				getResourceManagerFactory().getLoadEventListener().onLoad(event, LoadEventListener.IMMEDIATE_LOAD);

				return (T) event.getResult();
			}

			LoadEvent event = new LoadEvent(id, resourcePersister.getEntityName(), false, LocalResourceSession.this,
					false);
			boolean success = false;

			try {
				getResourceManagerFactory().getLoadEventListener().onLoad(event, LoadEventListener.IMMEDIATE_LOAD);
				success = true;
			} catch (ObjectNotFoundException onfe) {

			} finally {
				afterOperation(success);
			}

			return (T) event.getResult();
		}

		@Override
		public Optional<T> loadOptional(Serializable id) {
			// TODO Auto-generated method stub
			return Optional.ofNullable(getReference(id));
		}

	}

	private class LockRequestImpl implements LockRequest {

		private final LockOptions lockOptions;

		private LockRequestImpl(LockOptions lockOptions) {
			super();
			this.lockOptions = lockOptions;
		}

		@Override
		public LockMode getLockMode() {
			// TODO Auto-generated method stub
			return lockOptions.getLockMode();
		}

		@Override
		public LockRequest setLockMode(LockMode lockMode) {
			// TODO Auto-generated method stub
			lockOptions.setLockMode(lockMode);
			return this;
		}

		@Override
		public int getTimeOut() {
			// TODO Auto-generated method stub
			return lockOptions.getTimeOut();
		}

		@Override
		public LockRequest setTimeOut(int timeout) {
			// TODO Auto-generated method stub
			lockOptions.setTimeOut(timeout);
			return this;
		}

		@Override
		public boolean getScope() {
			// TODO Auto-generated method stub
			return lockOptions.getScope();
		}

		@Override
		public LockRequest setScope(boolean scope) {
			// TODO Auto-generated method stub
			lockOptions.setScope(scope);
			return this;
		}

		@Override
		public void lock(String entityName, Object object) {
			// TODO Auto-generated method stub
		}

		@Override
		public void lock(Object object) {
			// TODO Auto-generated method stub
		}

	}

	private <T> ResourcePersister<T> locatePersister(Class<T> clazz) {
		return getResourceManagerFactory().getResourcePersister(clazz);
	}

	private <T> ResourcePersister<T> locatePersister(String name) {
		return getResourceManagerFactory().getResourcePersister(name);
	}

}
