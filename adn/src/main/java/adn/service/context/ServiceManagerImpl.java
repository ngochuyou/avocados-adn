/**
 * 
 */
package adn.service.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;
import adn.service.context.transaction.Transaction;

/**
 * Simple implementation of {@link ServiceManager} that respects the
 * <a href="https://en.wikipedia.org/wiki/ACID">ACID</a> concept of application
 * resource management
 * 
 * <p>
 * This class consider application resources as {@link Resource}, and use a
 * {@link HashMap} as the persistence context
 * </p>
 * 
 * <p>
 * Initially, the persistence context supports:
 * <ul>
 * <li>{@link EntityResource}</li>
 * <li>{@link ObjectiveResource}</li>
 * </ul>
 * </p>
 * 
 * @author Ngoc Huy
 *
 */
@Order(6)
@Component
public class ServiceManagerImpl implements ServiceManager, ContextBuilder {

	private Map<Class<? extends Resource>, Map<String, Resource>> container;

	private PersisterChain persisterChain;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public ServiceManagerImpl() {
		// TODO Auto-generated constructor stub
		logger.debug("Constructing " + this.getClass());
		this.container = new HashMap<>();
		this.container.put(EntityResource.class, new HashMap<>());
		this.container.put(ObjectiveResource.class, new HashMap<>());
	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		// Initialise persisterChain
		// HIGHEST_PRESENDENCE is ObjectivePersister
		logger.debug("Initializing " + this.getClass());
		logger.debug("Adding " + ObjectivePersister.class.getName() + " to PersisterChain as HIGHEST_PRESENDENCE");
		this.persisterChain = new PersisterChainImpl(null,
				ContextProvider.getApplicationContext().getBean(ObjectivePersister.class));
		// the next one is ObjectivePersister
		logger.debug("Adding " + EntityPersister.class.getName() + " to PersisterChain as LOWEST_PRESENDENCE");
		this.persisterChain.register(ContextProvider.getApplicationContext().getBean(EntityPersister.class));
		logger.debug("Finished Initializing " + this.getClass());
	}

	@Override
	public void persist(Object o) {
		// TODO Auto-generated method stub
		Assert.notNull(o, "Cannot persisted null instance");

		Resource resource = persisterChain.persist(o);
		Map<String, Resource> resourceMap;

		if ((resourceMap = this.container.get(resource.getClass())).containsKey(resource.getId())) {
			throw new PersistenceException("There is already a resource with the same identifier: " + resource.getId());
		}

		resourceMap.put(resource.getId(), resource);
	}

	@Override
	public void detach(Object o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object load(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lock(Object id, LockMode mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Transaction openTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object generateResourceId() {
		// TODO Auto-generated method stub
		return UUID.randomUUID().toString();
	}

}
