/**
 * 
 */
package adn.service.resource.persister;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Type;

import adn.service.resource.ImmutableResourceEntryFactory;
import adn.service.resource.MutableResourceEntryFactory;
import adn.service.resource.ResourceEntryFactory;
import adn.service.resource.metamodel.ClassMetadata;
import adn.service.resource.metamodel.EntityMode;
import adn.service.resource.metamodel.ResourceMetamodel;
import adn.service.resource.metamodel.ResourceTypeImpl;
import adn.service.resource.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractResourcePersister implements ResourcePersister, ClassMetadata, Lockable {

	private final EntityManager resourceManager;

	private final int batchSize;

	private final ResourceMetamodel metamodel;
	private final ResourceTuplizer tuplizer;
	private final ResourceEntryFactory entryFactory;

	/**
	 * 
	 */
	public <X> AbstractResourcePersister(ResourceTypeImpl<X> type, EntityManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;

		batchSize = 1;

		metamodel = new ResourceMetamodel(type, this, this.resourceManager);
		tuplizer = metamodel.getTuplizer();
		entryFactory = metamodel.isMutable() ? MutableResourceEntryFactory.INSTANCE
				: ImmutableResourceEntryFactory.INSTANCE;
	}

	@Override
	public Boolean isTransient(Object object, EntityManager resourceManager) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getStorageSnapshot(Serializable id, EntityManager resourceManager) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * TODO: Retrieve the current version from the storage
	 */
	@Override
	public Object getCurrentVersion(Serializable id, EntityManager resourceManager) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	// locking implementations //
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	/**
	 * WARN: Duplicates or non-exist element checking is neglected
	 */
	@Override
	public int[] resolveAttributeIndices(String[] attributeNames) {
		// TODO Auto-generated method stub
		if (attributeNames == null || attributeNames.length == 0) {
			return new int[0];
		}

		int n;
		int[] indices = new int[n = attributeNames.length];

		for (int i = 0; i < n; i++) {
			indices[i] = metamodel.getPropertyIndex(attributeNames[i]);
		}

		return indices;
	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	// inserts //
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	/**
	 * perform insert and then retrieve the identifier
	 */
	@Override
	public Serializable insert(Object[] fields, Object object, EntityManager resourceManager)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insert(Serializable id, Object[] fields, Object object, EntityManager resourceManager)
			throws PersistenceException {
		// TODO Auto-generated method stub
	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	// updates //
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	@Override
	public void update(Serializable id, Object[] fields, int[] dirtyFields, boolean hasDirtyCollection,
			Object[] oldFields, Object oldVersion, Object object, Object rowId, EntityManager resourceManager)
			throws PersistenceException {
		// TODO Auto-generated method stub

	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	// deletes //
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	@Override
	public void delete(Serializable id, Object version, Object object, EntityManager resourceManager)
			throws PersistenceException {
		// TODO Auto-generated method stub

	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	// loads //
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */

	@Override
	public Object load(Serializable id, Object optionalObject, LockModeType lockMode, EntityManager resourceManager)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object load(Serializable id, Object optionalObject, LockModeType lockMode, EntityManager resourceManager,
			Boolean readOnly) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lock(Serializable id, Object version, Object object, LockModeType lockMode,
			EntityManager resourceManager) throws PersistenceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] findDirty(Object[] currentState, Object[] previousState, Object owner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] findModified(Object[] old, Object[] current, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> multiLoad(Serializable[] ids, EntityManager resourceManager, BatchLoadOptions loadOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPropertyIndex(String propertyName) {
		// TODO Auto-generated method stub
		return metamodel.getPropertyIndex(propertyName);
	}

	@Override
	public boolean isSubclassResourceName(String entityName) {
		// TODO Auto-generated method stub
		return metamodel.getSubclassEntityNames().contains(entityName);
	}

	@Override
	public String[] getPropertyNames() {
		// TODO Auto-generated method stub
		return metamodel.getPropertyNames();
	}

	@Override
	public Type<?>[] getPropertyTypes() {
		// TODO Auto-generated method stub
		return metamodel.getPropertyTypes();
	}

	@Override
	public void afterInitialize(Object entity, EntityManager resourceManager) {
		// TODO Auto-generated method stub
		tuplizer.afterInitialize(entity, resourceManager);
	}

	@Override
	public String getResourceName() {
		// TODO Auto-generated method stub
		return metamodel.getName();
	}

	@Override
	public String getRootResourceName() {
		// TODO Auto-generated method stub
		return metamodel.getRootName();
	}

	@Override
	public boolean isInherited() {
		// TODO Auto-generated method stub
		return metamodel.isInherited();
	}

	@Override
	public boolean isMutable() {
		// TODO Auto-generated method stub
		return metamodel.isMutable();
	}

	@Override
	public boolean isVersioned() {
		// TODO Auto-generated method stub
		return metamodel.isVersioned();
	}

	@Override
	public boolean isInstance(Object resource) {
		// TODO Auto-generated method stub
		return tuplizer.isInstance(resource);
	}

	@Override
	public boolean hasSubclasses() {
		// TODO Auto-generated method stub
		return metamodel.hasSubclasses();
	}

	@Override
	public boolean hasIdentifierProperty() {
		// TODO Auto-generated method stub
		return metamodel.getIdentifierProperty() != null && !metamodel.getIdentifierProperty().isVirtual();
	}

	@Override
	public boolean canExtractIdOutOfEntity() {
		// TODO Auto-generated method stub
		return hasIdentifierProperty() || hasEmbeddedCompositeIdentifier()
				|| metamodel.getIdentifierProperty().hasIdentifierMapper();
	}

	public boolean hasEmbeddedCompositeIdentifier() {
		return metamodel.getIdentifierProperty().isEmbedded();
	}

	@Override
	public boolean[] getPropertyCheckability() {
		// TODO Auto-generated method stub
		return metamodel.getPropertyCheckability();
	}

	@Override
	public boolean[] getPropertyNullability() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean[] getPropertyUpdatability() {
		// TODO Auto-generated method stub
		return metamodel.getPropertyUpdateability();
	}

	@Override
	public boolean[] getPropertyVersionability() {
		// TODO Auto-generated method stub
		return metamodel.getPropertyVersionability();
	}

	/**
	 * @return the resourceManager
	 */
	@Override
	public EntityManager getResourceManager() {
		return resourceManager;
	}

	@Override
	public ResourceEntryFactory getEntryFactory() {
		// TODO Auto-generated method stub
		return entryFactory;
	}

	@Override
	public EntityMode getEntityMode() {
		// TODO Auto-generated method stub
		return tuplizer.getEntityMode();
	}

	@Override
	public Object instantiate(Serializable id, EntityManager resourceManager) {
		// TODO Auto-generated method stub
		return tuplizer.instantiate(id, resourceManager);
	}

	@Override
	public ResourceTuplizer getEntityTuplizer() {
		// TODO Auto-generated method stub
		return tuplizer;
	}

	@Override
	public Serializable getIdentifier(Object resource) {
		// TODO Auto-generated method stub
		return tuplizer.getIdentifier(resource, null);
	}

	@Override
	public Serializable getIdentifier(Object resource, EntityManager resourceManager) {
		// TODO Auto-generated method stub
		return tuplizer.getIdentifier(resource, resourceManager);
	}

	@Override
	public IdentifierGenerator getIdentifierGenerator() {
		// TODO Auto-generated method stub
		return metamodel.getIdentifierProperty().getIdentifierGenerator();
	}

	@Override
	public String getIdentifierPropertyName() {
		// TODO Auto-generated method stub
		return metamodel.getIdentifierProperty().getName();
	}

	@Override
	public Type<?> getIdentifierType() {
		// TODO Auto-generated method stub
		return metamodel.getIdentifierProperty().getAttributeType();
	}

	@Override
	public Class<?> getMappedClass() {
		// TODO Auto-generated method stub
		return metamodel.getMappedClass();
	}

	@Override
	public Type<?> getPropertyType(String propertyName) throws IllegalStateException {
		// TODO Auto-generated method stub
		return metamodel.getPropertyType(propertyName);
	}

	@Override
	public Object getPropertyValue(Object resource, int i) throws PersistenceException {
		// TODO Auto-generated method stub
		return tuplizer.getPropertyValue(resource, getPropertyNames()[i]);
	}

	@Override
	public Object getPropertyValue(Object resource, String propertyName) {
		// TODO Auto-generated method stub
		return tuplizer.getPropertyValue(resource, propertyName);
	}

	@Override
	public Object[] getPropertyValues(Object resource) {
		// TODO Auto-generated method stub
		return tuplizer.getPropertyValues(resource);
	}

	@Override
	public ResourceMetamodel getResourceMetamodel() {
		// TODO Auto-generated method stub
		return metamodel;
	}

	@Override
	public Object getVersion(Object resource) throws PersistenceException {
		// TODO Auto-generated method stub
		return tuplizer.getVersion(resource);
	}

	@Override
	public int getVersionProperty() {
		// TODO Auto-generated method stub
		return metamodel.getVersionPropertyIndex();
	}

	@Override
	public Type<?> getVersionType() {
		// TODO Auto-generated method stub
		return metamodel.getVersionAttribute().getAttributeType();
	}

	@Override
	public ResourcePersister getSubclassResourcePersister(Object instance, EntityManager resourceManager) {
		// TODO Auto-generated method stub
		if (!hasSubclasses()) {
			return this;
		}
		// TODO: if not this, then ask the EntityManager for it
		return null;
	}

	@Override
	public void setIdentifier(Object resource, Serializable id, EntityManager resourceManager) {
		// TODO Auto-generated method stub
		tuplizer.setIdentifier(resource, id, resourceManager);
	}

	@Override
	public void setPropertyValue(Object resource, int i, Object value) {
		// TODO Auto-generated method stub
		tuplizer.setPropertyValue(resource, i, value);
	}

	@Override
	public void setPropertyValue(Object resource, String propertyName, Object value) throws PersistenceException {
		// TODO Auto-generated method stub
		tuplizer.setPropertyValue(resource, propertyName, value);
	}

	@Override
	public void setPropertyValues(Object resource, Object[] values) {
		// TODO Auto-generated method stub
		tuplizer.setPropertyValues(resource, values);
	}

	@Override
	public boolean isBatchLoadable() {
		// TODO Auto-generated method stub
		return batchSize > 1;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "AbstractResourcePersister for: " + metamodel.getName() + "\t" + metamodel.toString() + "\n"
				+ "\tEntryFactory: " + entryFactory.getClass().getName();
	}

}
