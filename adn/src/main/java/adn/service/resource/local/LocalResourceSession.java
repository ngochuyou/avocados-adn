/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

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

	private final EventFactory eventFactory = EventFactory.INSTANCE;

	private final ResourceContext resourceContext;

	private final ResourceManagerFactory resourceManagerFactory;

	private final LocalResourceStorage localStorage;

	private volatile boolean isRollbackOnly = false;

	
	
	/**
	 * 
	 */
	@Autowired
	public LocalResourceSession(
	// @formatter:off
			@NonNull final ResourceManagerFactory resourceManagerFactory,
			@NonNull final LocalResourceStorage localStorage) {
	// @formatter:on
		// TODO Auto-generated constructor stub
		this.resourceManagerFactory = resourceManagerFactory;
		this.localStorage = localStorage;
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

	@Override
	public <T> void manage(T instance, Class<T> type) {
		// TODO Auto-generated method stub
		eventFactory.createManageEvent(instance, type, this).fire();
	}

	@Override
	public <T> T find(Serializable identifier, Class<T> type) {
		// TODO Auto-generated method stub
		Object candidate = resourceContext.contains(identifier) ? resourceContext.find(identifier) : null;

		return unwrap(candidate, type);
	}

	@Override
	public <T> Serializable save(T instance) {
		// TODO Auto-generated method stub
		Class<T> type = unwrapType(instance.getClass());

		eventFactory.createSaveEvent(instance, type, this).fire();

		return resourceManagerFactory.locateResourceDescriptor(type).getIdentifier(instance);
	}

	@Override
	public LocalResourceStorage getLocalResourceStorage() {
		// TODO Auto-generated method stub
		return localStorage;
	}

	@Override
	public void setRollbackOnly() {
		// TODO Auto-generated method stub
		this.isRollbackOnly = true;
	}

	@SuppressWarnings("unchecked")
	protected <T> Class<T> unwrapType(Class<?> type) {

		return (Class<T>) type;
	}

	@SuppressWarnings("unchecked")
	protected <T> T unwrap(Object instance, Class<T> type) {
		if (instance == null || !instance.getClass().equals(type) || !type.isAssignableFrom(instance.getClass())) {
			return null;
		}

		return (T) instance;
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

}
