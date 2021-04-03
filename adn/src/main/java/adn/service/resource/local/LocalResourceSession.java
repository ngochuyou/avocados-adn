/**
 * 
 */
package adn.service.resource.local;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import adn.service.resource.event.EventFactory;

/**
 * @author Ngoc Huy
 *
 */
@Component
@RequestScope
@Lazy
public class LocalResourceSession implements ResourceManager {

	private ActionQueue actionQueue;

	private final EventFactory eventFactory = EventFactory.INSTANCE;

	private final ResourceContext resourceContext;

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

	private ResourceContext createResourceContext() {
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
	public ResourceContext getResourceContext() {
		// TODO Auto-generated method stub
		return resourceContext;
	}

	@Override
	public ActionQueue getActionQueue() {
		// TODO Auto-generated method stub
		return actionQueue;
	}

	@Override
	public void persist(Object entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T merge(T entity) {
		// TODO Auto-generated method stub
		return null;
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
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
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
	public EntityTransaction getTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}
