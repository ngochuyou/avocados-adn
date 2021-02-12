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

import adn.service.resource.ResourceEntryFactory;
import adn.service.resource.metamodel.EntityMode;
import adn.service.resource.metamodel.ResourceMetamodel;
import adn.service.resource.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister {

	String getRootResourceName();

	String getResourceName();

	ResourceMetamodel getResourceMetamodel();

	boolean isSubclassResourceName(String entityName);

	String[] getPropertyNames();

	int[] resolveAttributeIndexes(String[] attributeNames);

	Type<?> getPropertyType(String propertyName) throws IllegalStateException;

	Type<?>[] getPropertyTypes();

	int[] findDirty(Object[] currentState, Object[] previousState, Object owner);

	int[] findModified(Object[] old, Object[] current, Object object);

	boolean hasIdentifierProperty();

	String getIdentifierPropertyName();

	Type<?> getIdentifierType();

	boolean canExtractIdOutOfEntity();

	boolean isVersioned();

	boolean isMutable();

	Type<?> getVersionType();

	int getVersionProperty();

	IdentifierGenerator getIdentifierGenerator();

	Object getCurrentVersion(Serializable id, EntityManager resourceManager) throws PersistenceException;

	Object[] getDatabaseSnapshot(Serializable id, EntityManager resourceManager) throws PersistenceException;

	Object load(Serializable id, Object optionalObject, LockModeType lockMode, EntityManager resourceManager)
			throws PersistenceException;

	default Object load(Serializable id, Object optionalObject, LockModeType lockMode, EntityManager resourceManager,
			Boolean readOnly) throws PersistenceException {
		return load(id, optionalObject, lockMode, resourceManager);
	}

	boolean isBatchLoadable();

	List<Object> multiLoad(Serializable[] ids, EntityManager resourceManager, BatchLoadOptions loadOptions);

	void lock(Serializable id, Object version, Object object, LockModeType lockMode, EntityManager resourceManager)
			throws PersistenceException;

	void insert(Serializable id, Object[] fields, Object object, EntityManager resourceManager)
			throws PersistenceException;

	Serializable insert(Object[] fields, Object object, EntityManager resourceManager) throws PersistenceException;

	void delete(Serializable id, Object version, Object object, EntityManager resourceManager)
			throws PersistenceException;

	// @formatter:off
	void update(
			Serializable id,
			Object[] fields,
			int[] dirtyFields,
			boolean hasDirtyCollection,
			Object[] oldFields,
			Object oldVersion,
			Object object,
			Object rowId,
			EntityManager resourceManager
	) throws PersistenceException;
	// @formatter:on

	void afterInitialize(Object entity, EntityManager resourceManager);

	Boolean isTransient(Object object, EntityManager resourceManager) throws PersistenceException;

	Class<?> getMappedClass();

	void setPropertyValues(Object object, Object[] values);

	void setPropertyValue(Object object, int i, Object value);

	Object[] getPropertyValues(Object object);

	Object getPropertyValue(Object object, int i) throws PersistenceException;

	Object getPropertyValue(Object object, String propertyName);

	Serializable getIdentifier(Object entity);

	void setIdentifier(Object entity, Serializable id);

	Object getVersion(Object object) throws PersistenceException;

	Object instantiate(Serializable id, EntityManager resourceManager);

	boolean isInstance(Object object);

	ResourcePersister getSubclassEntityPersister(Object instance, EntityManager resourceManager);

	ResourceTuplizer getEntityTuplizer();

	ResourceEntryFactory getEntryFactory();
	
	EntityManager getResourceManager();

	EntityMode getEntityMode();
	
}
