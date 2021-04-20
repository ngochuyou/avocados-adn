/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.loading.internal.LoadContexts;
import org.hibernate.engine.spi.BatchFetchQueue;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.PersistentAttributeInterceptable;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceContextImpl implements ResourceContext {

	private static final int INIT_MAP_SIZE = 8;

	private final Map<ResourceKey<?>, Object> context = new HashMap<>(INIT_MAP_SIZE);

	private final Map<Object, ResourceEntry<?>> entryContext = new IdentityHashMap<>(INIT_MAP_SIZE);

	private final ResourceManager resourceManager;

	/**
	 * 
	 */
	public ResourceContextImpl(ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
	}

	@Override
	public Object find(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		if (context == null || context.isEmpty()) {
			return null;
		}

		return context.get(key);
	}

	@Override
	public void remove(Serializable pathName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

	// @formatter:off
	@Override
	public ResourceEntry<?> addEntry(
			Object instance,
			Status status,
			Object[] loadedState,
			Serializable id,
			LockMode lockMode,
			boolean isTransient,
			ResourcePersister<?> descriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceEntry<?> addResource(
			Object instance,
			Status status,
			Object[] loadedState,
			ResourceKey<?> key,
			LockMode lockMode,
			boolean isTransient,
			ResourcePersister<?> descriptor) {
		// TODO Auto-generated method stub
		return null;
	}
	// @formatter:on

	@Override
	public void setEntryStatus(ResourceEntry<?> entry, Status status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addResource(ResourceKey<?> key, Object instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getResource(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object removeResource(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceEntry<?> getEntry(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceEntry<?> removeEntry(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasEntry(Object instance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(ResourceKey<?> key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStateless() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SharedSessionContractImplementor getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LoadContexts getLoadContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addUnownedCollection(CollectionKey key, PersistentCollection collection) {
		// TODO Auto-generated method stub

	}

	@Override
	public PersistentCollection useUnownedCollection(CollectionKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BatchFetchQueue getBatchFetchQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNonReadOnlyEntities() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEntryStatus(EntityEntry entry, Status status) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTransactionCompletion() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getDatabaseSnapshot(Serializable id, EntityPersister persister) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getCachedDatabaseSnapshot(EntityKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getNaturalIdSnapshot(Serializable id, EntityPersister persister) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEntity(EntityKey key, Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getEntity(EntityKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsEntity(EntityKey key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object removeEntity(EntityKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEntity(EntityUniqueKey euk, Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getEntity(EntityUniqueKey euk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEntryFor(Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CollectionEntry getCollectionEntry(PersistentCollection coll) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityEntry addEntity(Object entity, Status status, Object[] loadedState, EntityKey entityKey,
			Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister,
			boolean disableVersionIncrement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityEntry addEntry(Object entity, Status status, Object[] loadedState, Object rowId, Serializable id,
			Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister,
			boolean disableVersionIncrement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsCollection(PersistentCollection collection) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsProxy(Object proxy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reassociateIfUninitializedProxy(Object value) throws MappingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reassociateProxy(Object value, Serializable id) throws MappingException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object unproxy(Object maybeProxy) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object unproxyAndReassociate(Object maybeProxy) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkUniqueness(EntityKey key, Object object) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object narrowProxy(Object proxy, EntityPersister persister, EntityKey key, Object object)
			throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object proxyFor(EntityPersister persister, EntityKey key, Object impl) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object proxyFor(Object impl) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEnhancedProxy(EntityKey key, PersistentAttributeInterceptable entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getCollectionOwner(Serializable key, CollectionPersister collectionPersister)
			throws MappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLoadedCollectionOwnerOrNull(PersistentCollection collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable getLoadedCollectionOwnerIdOrNull(PersistentCollection collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addUninitializedCollection(CollectionPersister persister, PersistentCollection collection,
			Serializable id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addUninitializedDetachedCollection(CollectionPersister persister, PersistentCollection collection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNewCollection(CollectionPersister persister, PersistentCollection collection)
			throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInitializedDetachedCollection(CollectionPersister collectionPersister,
			PersistentCollection collection) throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public CollectionEntry addInitializedCollection(CollectionPersister persister, PersistentCollection collection,
			Serializable id) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistentCollection getCollection(CollectionKey collectionKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNonLazyCollection(PersistentCollection collection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeNonLazyCollections() throws HibernateException {
		// TODO Auto-generated method stub

	}

	@Override
	public PersistentCollection getCollectionHolder(Object array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCollectionHolder(PersistentCollection holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public PersistentCollection removeCollectionHolder(Object array) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable getSnapshot(PersistentCollection coll) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionEntry getCollectionEntryOrNull(Object collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getProxy(EntityKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addProxy(EntityKey key, Object proxy) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object removeProxy(EntityKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashSet getNullifiableEntityKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getEntitiesByKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry<Object, EntityEntry>[] reentrantSafeEntityEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getEntityEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfManagedEntities() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map getCollectionEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forEachCollectionEntry(BiConsumer<PersistentCollection, CollectionEntry> action, boolean concurrent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map getCollectionsByKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCascadeLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int incrementCascadeLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int decrementCascadeLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFlushing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFlushing(boolean flushing) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeLoad() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterLoad() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLoadFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Serializable getOwnerId(String entityName, String propertyName, Object childEntity, Map mergeMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getIndexInOwner(String entity, String property, Object childObject, Map mergeMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNullProperty(EntityKey ownerKey, String propertyName) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPropertyNull(EntityKey ownerKey, String propertyName) {
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
	public boolean isReadOnly(Object entityOrProxy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadOnly(Object entityOrProxy, boolean readOnly) {
		// TODO Auto-generated method stub

	}

	@Override
	public void replaceDelayedEntityIdentityInsertKeys(EntityKey oldKey, Serializable generatedId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChildParent(Object child, Object parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeChildParent(Object child) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerInsertedKey(EntityPersister persister, Serializable id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean wasInsertedDuringTransaction(EntityPersister persister, Serializable id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsNullifiableEntityKey(Supplier<EntityKey> sek) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerNullifiableEntityKey(EntityKey key) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isNullifiableEntityKeysEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCollectionEntriesSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CollectionEntry removeCollectionEntry(PersistentCollection collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearCollectionsByKey() {
		// TODO Auto-generated method stub

	}

	@Override
	public PersistentCollection addCollectionByKey(CollectionKey collectionKey,
			PersistentCollection persistentCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeCollectionByKey(CollectionKey collectionKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator managedEntitiesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NaturalIdHelper getNaturalIdHelper() {
		// TODO Auto-generated method stub
		return null;
	}

}
