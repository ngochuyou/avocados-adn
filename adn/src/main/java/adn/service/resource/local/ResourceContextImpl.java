/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.EntityEntryContext;
import org.hibernate.engine.loading.internal.LoadContexts;
import org.hibernate.engine.spi.BatchFetchQueue;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.PersistentAttributeInterceptable;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceContextImpl implements PersistenceContext {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final int INIT_MAP_SIZE = 8;

	private final Map<EntityKey, Object> context = new HashMap<>(INIT_MAP_SIZE);
	private EntityEntryContext entryContext;

	private final ResourceManager resourceManager;
	private boolean hasNonReadOnlyEntities;

	private final Observer observer = new Observer();

	public ResourceContextImpl(ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
	}

	@Override
	public boolean isStateless() {

		return false;
	}

	@Override
	public SharedSessionContractImplementor getSession() {

		return null;
	}

	@Override
	public LoadContexts getLoadContexts() {

		return null;
	}

	@Override
	public void addUnownedCollection(CollectionKey key, PersistentCollection collection) {

	}

	@Override
	public PersistentCollection useUnownedCollection(CollectionKey key) {

		return null;
	}

	@Override
	public BatchFetchQueue getBatchFetchQueue() {

		return null;
	}

	@Override
	public void clear() {

	}

	private void setHasNonReadOnlyEnties(Status status) {
		if (status == Status.DELETED || status == Status.MANAGED || status == Status.SAVING) {
			hasNonReadOnlyEntities = true;
		}
	}

	@Override
	public boolean hasNonReadOnlyEntities() {

		return hasNonReadOnlyEntities;
	}

	@Override
	public void setEntryStatus(EntityEntry entry, Status status) {

	}

	@Override
	public void afterTransactionCompletion() {

	}

	@Override
	public Object[] getDatabaseSnapshot(Serializable id, EntityPersister persister) {

		return null;
	}

	@Override
	public Object[] getCachedDatabaseSnapshot(EntityKey key) {

		return null;
	}

	@Override
	public Object[] getNaturalIdSnapshot(Serializable id, EntityPersister persister) {

		return null;
	}

	@Override
	public void addEntity(EntityKey key, Object entity) {

	}

	@Override
	public Object getEntity(EntityKey key) {

		return null;
	}

	@Override
	public boolean containsEntity(EntityKey key) {

		return false;
	}

	@Override
	public Object removeEntity(EntityKey key) {

		return null;
	}

	@Override
	public void addEntity(EntityUniqueKey euk, Object entity) {

	}

	@Override
	public Object getEntity(EntityUniqueKey euk) {

		return null;
	}

	@Override
	public EntityEntry getEntry(Object entity) {

		return null;
	}

	@Override
	public EntityEntry removeEntry(Object entity) {

		return null;
	}

	@Override
	public boolean isEntryFor(Object entity) {

		return false;
	}

	@Override
	public CollectionEntry getCollectionEntry(PersistentCollection coll) {

		return null;
	}

	@Override
	public EntityEntry addEntity(Object entity, Status status, Object[] loadedState, EntityKey entityKey,
			Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister,
			boolean disableVersionIncrement) {

		return null;
	}

	@Override
	public ResourceEntry<?> addEntry(Object entity, Status status, Object[] loadedState, Object rowId, Serializable id,
			Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister,
			boolean disableVersionIncrement) {

		ResourceEntry<?> entry = (ResourceEntry<?>) persister.getEntityEntryFactory().createEntityEntry(status,
				loadedState, rowId, id, version, lockMode, existsInDatabase, persister, disableVersionIncrement, this);

		entryContext.addEntityEntry(entity, entry);
		setHasNonReadOnlyEnties(status);
		observer.onChange();

		return entry;
	}

	@Override
	public boolean containsCollection(PersistentCollection collection) {

		return false;
	}

	@Override
	public boolean containsProxy(Object proxy) {

		return false;
	}

	@Override
	public boolean reassociateIfUninitializedProxy(Object value) throws MappingException {

		return false;
	}

	@Override
	public void reassociateProxy(Object value, Serializable id) throws MappingException {

	}

	@Override
	public Object unproxy(Object maybeProxy) throws HibernateException {

		return null;
	}

	@Override
	public Object unproxyAndReassociate(Object maybeProxy) throws HibernateException {

		return null;
	}

	@Override
	public void checkUniqueness(EntityKey key, Object object) throws HibernateException {

	}

	@Override
	public Object narrowProxy(Object proxy, EntityPersister persister, EntityKey key, Object object)
			throws HibernateException {

		return null;
	}

	@Override
	public Object proxyFor(EntityPersister persister, EntityKey key, Object impl) throws HibernateException {

		return null;
	}

	@Override
	public Object proxyFor(Object impl) throws HibernateException {

		return null;
	}

	@Override
	public void addEnhancedProxy(EntityKey key, PersistentAttributeInterceptable entity) {

	}

	@Override
	public Object getCollectionOwner(Serializable key, CollectionPersister collectionPersister)
			throws MappingException {

		return null;
	}

	@Override
	public Object getLoadedCollectionOwnerOrNull(PersistentCollection collection) {

		return null;
	}

	@Override
	public Serializable getLoadedCollectionOwnerIdOrNull(PersistentCollection collection) {

		return null;
	}

	@Override
	public void addUninitializedCollection(CollectionPersister persister, PersistentCollection collection,
			Serializable id) {

	}

	@Override
	public void addUninitializedDetachedCollection(CollectionPersister persister, PersistentCollection collection) {

	}

	@Override
	public void addNewCollection(CollectionPersister persister, PersistentCollection collection)
			throws HibernateException {

	}

	@Override
	public void addInitializedDetachedCollection(CollectionPersister collectionPersister,
			PersistentCollection collection) throws HibernateException {

	}

	@Override
	public CollectionEntry addInitializedCollection(CollectionPersister persister, PersistentCollection collection,
			Serializable id) throws HibernateException {

		return null;
	}

	@Override
	public PersistentCollection getCollection(CollectionKey collectionKey) {

		return null;
	}

	@Override
	public void addNonLazyCollection(PersistentCollection collection) {

	}

	@Override
	public void initializeNonLazyCollections() throws HibernateException {

	}

	@Override
	public PersistentCollection getCollectionHolder(Object array) {

		return null;
	}

	@Override
	public void addCollectionHolder(PersistentCollection holder) {

	}

	@Override
	public PersistentCollection removeCollectionHolder(Object array) {

		return null;
	}

	@Override
	public Serializable getSnapshot(PersistentCollection coll) {

		return null;
	}

	@Override
	public CollectionEntry getCollectionEntryOrNull(Object collection) {

		return null;
	}

	@Override
	public Object getProxy(EntityKey key) {

		return null;
	}

	@Override
	public void addProxy(EntityKey key, Object proxy) {

	}

	@Override
	public Object removeProxy(EntityKey key) {

		return null;
	}

	@Override
	public HashSet getNullifiableEntityKeys() {

		return null;
	}

	@Override
	public Map getEntitiesByKey() {

		return null;
	}

	@Override
	public Entry<Object, EntityEntry>[] reentrantSafeEntityEntries() {

		return null;
	}

	@Override
	public Map getEntityEntries() {

		return null;
	}

	@Override
	public int getNumberOfManagedEntities() {

		return 0;
	}

	@Override
	public Map getCollectionEntries() {

		return null;
	}

	@Override
	public void forEachCollectionEntry(BiConsumer<PersistentCollection, CollectionEntry> action, boolean concurrent) {

	}

	@Override
	public Map getCollectionsByKey() {

		return null;
	}

	@Override
	public int getCascadeLevel() {

		return 0;
	}

	@Override
	public int incrementCascadeLevel() {

		return 0;
	}

	@Override
	public int decrementCascadeLevel() {

		return 0;
	}

	@Override
	public boolean isFlushing() {

		return false;
	}

	@Override
	public void setFlushing(boolean flushing) {

	}

	@Override
	public void beforeLoad() {

	}

	@Override
	public void afterLoad() {

	}

	@Override
	public boolean isLoadFinished() {

		return false;
	}

	@Override
	public Serializable getOwnerId(String entityName, String propertyName, Object childEntity, Map mergeMap) {

		return null;
	}

	@Override
	public Object getIndexInOwner(String entity, String property, Object childObject, Map mergeMap) {

		return null;
	}

	@Override
	public void addNullProperty(EntityKey ownerKey, String propertyName) {

	}

	@Override
	public boolean isPropertyNull(EntityKey ownerKey, String propertyName) {

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
	public boolean isReadOnly(Object entityOrProxy) {

		return false;
	}

	@Override
	public void setReadOnly(Object entityOrProxy, boolean readOnly) {

	}

	@Override
	public void replaceDelayedEntityIdentityInsertKeys(EntityKey oldKey, Serializable generatedId) {

	}

	@Override
	public void addChildParent(Object child, Object parent) {

	}

	@Override
	public void removeChildParent(Object child) {

	}

	@Override
	public void registerInsertedKey(EntityPersister persister, Serializable id) {

	}

	@Override
	public boolean wasInsertedDuringTransaction(EntityPersister persister, Serializable id) {

		return false;
	}

	@Override
	public boolean containsNullifiableEntityKey(Supplier<EntityKey> sek) {

		return false;
	}

	@Override
	public void registerNullifiableEntityKey(EntityKey key) {

	}

	@Override
	public boolean isNullifiableEntityKeysEmpty() {

		return false;
	}

	@Override
	public int getCollectionEntriesSize() {

		return 0;
	}

	@Override
	public CollectionEntry removeCollectionEntry(PersistentCollection collection) {

		return null;
	}

	@Override
	public void clearCollectionsByKey() {

	}

	@Override
	public PersistentCollection addCollectionByKey(CollectionKey collectionKey,
			PersistentCollection persistentCollection) {

		return null;
	}

	@Override
	public void removeCollectionByKey(CollectionKey collectionKey) {

	}

	@Override
	public Iterator managedEntitiesIterator() {

		return null;
	}

	@Override
	public NaturalIdHelper getNaturalIdHelper() {

		return null;
	}

	private class Observer {

		void onChange() {
			// @formatter:off
			logger.debug(String.format("PersistenceContext has changed\n"
					+ "Entries: \n%s",
					Stream.of(context)
						.map(instance -> entryContext.getEntityEntry(instance))
						.map(entry -> "\t" + entry.toString()).collect(Collectors.joining("\n"))));
			// @formatter:on
		}

	}

}
