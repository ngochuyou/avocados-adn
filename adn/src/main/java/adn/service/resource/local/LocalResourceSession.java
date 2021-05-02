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

	/**
	 * 
	 */
	@Autowired
	public LocalResourceSession(
	// @formatter:off
			@NonNull final ResourceManagerFactory resourceManagerFactory) {
	// @formatter:on

		this.resourceManagerFactory = resourceManagerFactory;
		resourceContext = createResourceContext();
	}

	private PersistenceContext createResourceContext() {
		return new ResourceContextImpl(this);
	}

	@Override
	public ResourceManagerFactory getResourceManagerFactory() {
		return resourceManagerFactory;
	}

	@Override
	public void persist(Object entity) {

	}

	@Override
	public void remove(Object entity) {

	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {

		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {

		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {

		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {

		return null;
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {

		return null;
	}

	@Override
	public void flush() {

	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {

	}

	@Override
	public FlushModeType getFlushMode() {

		return null;
	}

	@Override
	public void lock(Object entity, LockModeType lockMode) {

	}

	@Override
	public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {

	}

	@Override
	public void refresh(Object entity) {

	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode) {

	}

	@Override
	public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {

	}

	@Override
	public void clear() {

	}

	@Override
	public void detach(Object entity) {

	}

	@Override
	public boolean contains(Object entity) {

		return false;
	}

	@Override
	public LockModeType getLockMode(Object entity) {

		return null;
	}

	@Override
	public void setProperty(String propertyName, Object value) {

	}

	@Override
	public Map<String, Object> getProperties() {

		return null;
	}

	@Override
	public void joinTransaction() {

	}

	@Override
	public boolean isJoinedToTransaction() {

		return false;
	}

	@Override
	public Object getDelegate() {

		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean isOpen() {

		return false;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {

		return null;
	}

	@Override
	public SessionFactoryImplementor getFactory() {

		return null;
	}

	@Override
	public SessionEventListenerManager getEventListenerManager() {

		return null;
	}

	@Override
	public JdbcCoordinator getJdbcCoordinator() {

		return null;
	}

	@Override
	public JdbcServices getJdbcServices() {

		return null;
	}

	@Override
	public String getTenantIdentifier() {

		return null;
	}

	@Override
	public UUID getSessionIdentifier() {

		return null;
	}

	@Override
	public boolean isClosed() {

		return false;
	}

	@Override
	public void checkOpen(boolean markForRollbackIfClosed) {

	}

	@Override
	public void markForRollbackOnly() {

	}

	@Override
	public long getTransactionStartTimestamp() {

		return 0;
	}

	@Override
	public CacheTransactionSynchronization getCacheTransactionSynchronization() {

		return null;
	}

	@Override
	public boolean isTransactionInProgress() {

		return false;
	}

	@Override
	public Transaction accessTransaction() {

		return null;
	}

	@Override
	public EntityKey generateEntityKey(Serializable id, EntityPersister persister) {

		return new EntityKey(id, persister);
	}

	@Override
	public Interceptor getInterceptor() {

		return null;
	}

	@Override
	public void setAutoClear(boolean enabled) {

	}

	@Override
	public void initializeCollection(PersistentCollection collection, boolean writing) throws HibernateException {

	}

	@Override
	public Object internalLoad(String entityName, Serializable id, boolean eager, boolean nullable)
			throws HibernateException {

		return null;
	}

	@Override
	public Object immediateLoad(String entityName, Serializable id) throws HibernateException {

		return null;
	}

	@Override
	public List list(String query, QueryParameters queryParameters) throws HibernateException {

		return null;
	}

	@Override
	public Iterator iterate(String query, QueryParameters queryParameters) throws HibernateException {

		return null;
	}

	@Override
	public ScrollableResultsImplementor scroll(String query, QueryParameters queryParameters)
			throws HibernateException {

		return null;
	}

	@Override
	public ScrollableResultsImplementor scroll(Criteria criteria, ScrollMode scrollMode) {

		return null;
	}

	@Override
	public List list(Criteria criteria) {

		return null;
	}

	@Override
	public List listFilter(Object collection, String filter, QueryParameters queryParameters)
			throws HibernateException {

		return null;
	}

	@Override
	public Iterator iterateFilter(Object collection, String filter, QueryParameters queryParameters)
			throws HibernateException {

		return null;
	}

	@Override
	public EntityPersister getEntityPersister(String entityName, Object object) throws HibernateException {

		return null;
	}

	@Override
	public Object getEntityUsingInterceptor(EntityKey key) throws HibernateException {

		return null;
	}

	@Override
	public Serializable getContextEntityIdentifier(Object object) {

		return null;
	}

	@Override
	public String bestGuessEntityName(Object object) {

		return null;
	}

	@Override
	public String guessEntityName(Object entity) throws HibernateException {

		return null;
	}

	@Override
	public Object instantiate(String entityName, Serializable id) throws HibernateException {

		return null;
	}

	@Override
	public List listCustomQuery(CustomQuery customQuery, QueryParameters queryParameters) throws HibernateException {

		return null;
	}

	@Override
	public List list(NativeSQLQuerySpecification spec, QueryParameters queryParameters) throws HibernateException {

		return null;
	}

	@Override
	public int getDontFlushFromFind() {

		return 0;
	}

	@Override
	public int executeUpdate(String query, QueryParameters queryParameters) throws HibernateException {

		return 0;
	}

	@Override
	public int executeNativeUpdate(NativeSQLQuerySpecification specification, QueryParameters queryParameters)
			throws HibernateException {

		return 0;
	}

	@Override
	public CacheMode getCacheMode() {

		return null;
	}

	@Override
	public FlushMode getHibernateFlushMode() {

		return null;
	}

	@Override
	public Connection connection() {

		return null;
	}

	@Override
	public boolean isEventSource() {

		return true;
	}

	@Override
	public void afterScrollOperation() {

	}

	@Override
	public <T> QueryImplementor<T> createQuery(String arg0, Class<T> arg1, Selection arg2, QueryOptions arg3) {

		return null;
	}

	@Override
	public ScrollableResultsImplementor scrollCustomQuery(CustomQuery customQuery, QueryParameters queryParameters)
			throws HibernateException {

		return null;
	}

	@Override
	public ScrollableResultsImplementor scroll(NativeSQLQuerySpecification spec, QueryParameters queryParameters) {

		return null;
	}

	@Override
	public void setCacheMode(CacheMode cm) {

	}

	@Override
	public void setFlushMode(FlushMode flushMode) {

	}

	@Override
	public void setHibernateFlushMode(FlushMode flushMode) {

	}

	@Override
	public boolean shouldAutoClose() {

		return false;
	}

	@Override
	public boolean isAutoCloseSessionEnabled() {

		return false;
	}

	@Override
	public LoadQueryInfluencers getLoadQueryInfluencers() {

		return null;
	}

	@Override
	public ExceptionConverter getExceptionConverter() {

		return null;
	}

	@Override
	public PersistenceContext getPersistenceContextInternal() {

		return resourceContext;
	}

	@Override
	public boolean isConnected() {

		return false;
	}

	@Override
	public Transaction beginTransaction() {

		return null;
	}

	@Override
	public Transaction getTransaction() {

		return null;
	}

	@Override
	public ProcedureCall getNamedProcedureCall(String name) {

		return null;
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName) {

		return null;
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {

		return null;
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {

		return null;
	}

	@Override
	public Criteria createCriteria(Class persistentClass) {

		return null;
	}

	@Override
	public Criteria createCriteria(Class persistentClass, String alias) {

		return null;
	}

	@Override
	public Criteria createCriteria(String entityName) {

		return null;
	}

	@Override
	public Criteria createCriteria(String entityName, String alias) {

		return null;
	}

	@Override
	public Integer getJdbcBatchSize() {

		return null;
	}

	@Override
	public void setJdbcBatchSize(Integer jdbcBatchSize) {

	}

	@Override
	public JdbcSessionContext getJdbcSessionContext() {

		return null;
	}

	@Override
	public JdbcConnectionAccess getJdbcConnectionAccess() {

		return null;
	}

	@Override
	public TransactionCoordinator getTransactionCoordinator() {

		return null;
	}

	@Override
	public void startTransactionBoundary() {

	}

	@Override
	public void afterTransactionBegin() {

	}

	@Override
	public void beforeTransactionCompletion() {

	}

	@Override
	public void afterTransactionCompletion(boolean successful, boolean delayed) {

	}

	@Override
	public void flushBeforeTransactionCompletion() {

	}

	@Override
	public boolean shouldAutoJoinTransaction() {

		return false;
	}

	@Override
	public <T> T execute(Callback<T> callback) {

		return null;
	}

	@Override
	public boolean useStreamForLobBinding() {

		return false;
	}

	@Override
	public LobCreator getLobCreator() {

		return null;
	}

	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		if (!sqlTypeDescriptor.canBeRemapped()) {
			return sqlTypeDescriptor;
		}

		SqlTypeDescriptor remapped = resourceManagerFactory.getDialect().remapSqlTypeDescriptor(sqlTypeDescriptor);

		return remapped == null ? sqlTypeDescriptor : remapped;
	}

	@Override
	public TimeZone getJdbcTimeZone() {
		return null;
	}

	@Override
	public QueryImplementor getNamedQuery(String queryName) {
		return null;
	}

	@Override
	public QueryImplementor createQuery(String queryString) {

		return null;
	}

	@Override
	public <R> QueryImplementor<R> createQuery(String queryString, Class<R> resultClass) {

		return null;
	}

	@Override
	public <R> QueryImplementor<R> createNamedQuery(String name, Class<R> resultClass) {

		return null;
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString) {

		return null;
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString, Class resultClass) {

		return null;
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString, String resultSetMapping) {

		return null;
	}

	@Override
	public NativeQueryImplementor getNamedNativeQuery(String name) {

		return null;
	}

	@Override
	public Metamodel getMetamodel() {

		return getResourceManagerFactory().getMetamodel();
	}

	@Override
	public PersistenceContext getPersistenceContext() {

		return resourceContext;
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {

		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {

		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {

		return null;
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {

		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {

		return null;
	}

	@Override
	public SharedSessionBuilder sessionWithOptions() {

		return null;
	}

	@Override
	public void cancelQuery() throws HibernateException {

	}

	@Override
	public boolean isDirty() throws HibernateException {

		return false;
	}

	@Override
	public boolean isDefaultReadOnly() {

		return false;
	}

	@Override
	public void setDefaultReadOnly(boolean readOnly) {

	}

	@Override
	public Serializable getIdentifier(Object object) {

		return null;
	}

	@Override
	public boolean contains(String entityName, Object object) {

		return false;
	}

	@Override
	public void evict(Object object) {

	}

	@Override
	public <T> T load(Class<T> theClass, Serializable id, LockMode lockMode) {

		return null;
	}

	@Override
	public <T> T load(Class<T> theClass, Serializable id, LockOptions lockOptions) {

		return null;
	}

	@Override
	public Object load(String entityName, Serializable id, LockMode lockMode) {

		return null;
	}

	@Override
	public Object load(String entityName, Serializable id, LockOptions lockOptions) {

		return null;
	}

	@Override
	public <T> T load(Class<T> theClass, Serializable id) {

		return byId(theClass).getReference(id);
	}

	@Override
	public Object load(String entityName, Serializable id) {

		return byId(entityName).getReference(id);
	}

	@Override
	public void load(Object object, Serializable id) {

	}

	@Override
	public void replicate(Object object, ReplicationMode replicationMode) {

	}

	@Override
	public void replicate(String entityName, Object object, ReplicationMode replicationMode) {

	}

	@Override
	public Serializable save(Object object) {

		return null;
	}

	@Override
	public Serializable save(String entityName, Object object) {

		return null;
	}

	@Override
	public void saveOrUpdate(Object object) {

	}

	@Override
	public void saveOrUpdate(String entityName, Object object) {

	}

	@Override
	public void update(Object object) {

	}

	@Override
	public void update(String entityName, Object object) {

	}

	@Override
	public Object merge(String entityName, Object object) {

		return null;
	}

	@Override
	public void persist(String entityName, Object object) {

	}

	@Override
	public void delete(Object object) {

	}

	@Override
	public void delete(String entityName, Object object) {

	}

	@Override
	public void lock(Object object, LockMode lockMode) {

	}

	@Override
	public void lock(String entityName, Object object, LockMode lockMode) {

	}

	@Override
	public LockRequest buildLockRequest(LockOptions lockOptions) {

		return new LockRequestImpl(lockOptions);
	}

	@Override
	public void refresh(String entityName, Object object) {

	}

	@Override
	public void refresh(Object object, LockMode lockMode) {

	}

	@Override
	public void refresh(Object object, LockOptions lockOptions) {

	}

	@Override
	public void refresh(String entityName, Object object, LockOptions lockOptions) {

	}

	@Override
	public LockMode getCurrentLockMode(Object object) {

		return null;
	}

	@Override
	public org.hibernate.Query createFilter(Object collection, String queryString) {

		return null;
	}

	@Override
	public <T> T get(Class<T> entityType, Serializable id) {

		return byId(entityType).load(id);
	}

	@Override
	public <T> T get(Class<T> entityType, Serializable id, LockMode lockMode) {

		return null;
	}

	@Override
	public <T> T get(Class<T> entityType, Serializable id, LockOptions lockOptions) {

		return null;
	}

	@Override
	public Object get(String entityName, Serializable id) {

		return byId(entityName).load(id);
	}

	@Override
	public Object get(String entityName, Serializable id, LockMode lockMode) {

		return null;
	}

	@Override
	public Object get(String entityName, Serializable id, LockOptions lockOptions) {

		return null;
	}

	@Override
	public String getEntityName(Object object) {

		return null;
	}

	@Override
	public IdentifierLoadAccess<?> byId(String entityName) {

		return new IdentifierLoadAccessImpl<>(locatePersister(entityName));
	}

	@Override
	public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {

		return null;
	}

	@Override
	public MultiIdentifierLoadAccess byMultipleIds(String entityName) {

		return null;
	}

	@Override
	public <T> IdentifierLoadAccess<T> byId(Class<T> entityClass) {
		return new IdentifierLoadAccessImpl<>(locatePersister(entityClass));
	}

	@Override
	public NaturalIdLoadAccess byNaturalId(String entityName) {

		return null;
	}

	@Override
	public <T> NaturalIdLoadAccess<T> byNaturalId(Class<T> entityClass) {

		return null;
	}

	@Override
	public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {

		return null;
	}

	@Override
	public <T> SimpleNaturalIdLoadAccess<T> bySimpleNaturalId(Class<T> entityClass) {

		return null;
	}

	@Override
	public Filter enableFilter(String filterName) {

		return null;
	}

	@Override
	public Filter getEnabledFilter(String filterName) {

		return null;
	}

	@Override
	public void disableFilter(String filterName) {

	}

	@Override
	public SessionStatistics getStatistics() {

		return null;
	}

	@Override
	public boolean isReadOnly(Object entityOrProxy) {

		return false;
	}

	@Override
	public void setReadOnly(Object entityOrProxy, boolean readOnly) {

	}

	@Override
	public Connection disconnect() {

		return null;
	}

	@Override
	public void reconnect(Connection connection) {

	}

	@Override
	public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {

		return false;
	}

	@Override
	public void enableFetchProfile(String name) throws UnknownProfileException {

	}

	@Override
	public void disableFetchProfile(String name) throws UnknownProfileException {

	}

	@Override
	public TypeHelper getTypeHelper() {

		return null;
	}

	@Override
	public LobHelper getLobHelper() {

		return null;
	}

	@Override
	public void addEventListeners(SessionEventListener... listeners) {

	}

	@Override
	public Object merge(Object object) {

		return null;
	}

	@Override
	public SessionImplementor getSession() {

		return null;
	}

	@Override
	public LockOptions getLockRequest(LockModeType lockModeType, Map<String, Object> properties) {

		return null;
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {

		return null;
	}

	@Override
	public <T> RootGraphImplementor<T> createEntityGraph(Class<T> rootType) {

		return null;
	}

	@Override
	public RootGraphImplementor<?> createEntityGraph(String graphName) {

		return null;
	}

	@Override
	public RootGraphImplementor<?> getEntityGraph(String graphName) {

		return null;
	}

	@Override
	public boolean isFlushBeforeCompletionEnabled() {

		return false;
	}

	@Override
	public ActionQueue getActionQueue() {

		return null;
	}

	@Override
	public Object instantiate(EntityPersister persister, Serializable id) throws HibernateException {

		return null;
	}

	@Override
	public void forceFlush(EntityEntry e) throws HibernateException {

	}

	@Override
	public QueryImplementor createNamedQuery(String name) {

		return null;
	}

	@Override
	public NativeQueryImplementor createSQLQuery(String sqlString) {

		return null;
	}

	@Override
	public NativeQueryImplementor getNamedSQLQuery(String name) {

		return null;
	}

	@Override
	public <T> QueryImplementor<T> createQuery(CriteriaQuery<T> criteriaQuery) {

		return null;
	}

	@Override
	public QueryImplementor createQuery(CriteriaUpdate updateQuery) {

		return null;
	}

	@Override
	public QueryImplementor createQuery(CriteriaDelete deleteQuery) {

		return null;
	}

	@Override
	public void merge(String entityName, Object object, Map copiedAlready) throws HibernateException {

	}

	@Override
	public void persist(String entityName, Object object, Map createdAlready) throws HibernateException {

	}

	@Override
	public void persistOnFlush(String entityName, Object object, Map copiedAlready) {

	}

	@Override
	public void refresh(String entityName, Object object, Map refreshedAlready) throws HibernateException {

	}

	@Override
	public void delete(String entityName, Object child, boolean isCascadeDeleteEnabled, Set transientEntities) {

	}

	@Override
	public void removeOrphanBeforeUpdates(String entityName, Object child) {

	}

	public void afterOperation(boolean success) {
		logger.debug("Operation status: " + success);
//		if (!isTransactionInProgress()) {
//			getJdbcCoordinator().afterTransaction();
//		}
	}

	@Override
	public <T> T unwrap(Class<T> cls) {

		return (T) this;
	}

	private class IdentifierLoadAccessImpl<T> implements IdentifierLoadAccess<T> {

		private final ResourcePersister<T> resourcePersister;

		private LockOptions lockOptions;

		IdentifierLoadAccessImpl(ResourcePersister<T> resourcePersister) {
			this.resourcePersister = resourcePersister;
		}

		@Override
		public IdentifierLoadAccess<T> with(LockOptions lockOptions) {

			this.lockOptions = lockOptions;
			return this;
		}

		@Override
		@Deprecated
		public IdentifierLoadAccess<T> with(CacheMode cacheMode) {

			return this;
		}

		@Override
		@Deprecated
		public IdentifierLoadAccess<T> with(RootGraph<T> graph, GraphSemantic semantic) {

			return this;
		}

		@Override
		public T getReference(Serializable id) {
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

			return lockOptions.getLockMode();
		}

		@Override
		public LockRequest setLockMode(LockMode lockMode) {

			lockOptions.setLockMode(lockMode);
			return this;
		}

		@Override
		public int getTimeOut() {

			return lockOptions.getTimeOut();
		}

		@Override
		public LockRequest setTimeOut(int timeout) {

			lockOptions.setTimeOut(timeout);
			return this;
		}

		@Override
		public boolean getScope() {

			return lockOptions.getScope();
		}

		@Override
		public LockRequest setScope(boolean scope) {

			lockOptions.setScope(scope);
			return this;
		}

		@Override
		public void lock(String entityName, Object object) {

		}

		@Override
		public void lock(Object object) {

		}

	}

	private <T> ResourcePersister<T> locatePersister(Class<T> clazz) {
		return (ResourcePersister<T>) getResourceManagerFactory().getMetamodel().entityPersister(clazz);
	}

	private <T> ResourcePersister<T> locatePersister(String name) {
		return (ResourcePersister<T>) getResourceManagerFactory().getMetamodel().entityPersister(name);
	}

}
