/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * @author Ngoc Huy
 *
 */
@Lazy
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class ResourceContextImpl implements ResourcePersistenceContext {

	private Map<Serializable, Object> context;

	private final ResourceManager resourceManager;

	private final Serializable id;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 */
	public ResourceContextImpl(ResourceManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
		this.id = Thread.currentThread().getId();
	}

	@Override
	public Object find(String pathName) {
		// TODO Auto-generated method stub
		if (context == null || context.isEmpty()) {
			return null;
		}

		return context.getOrDefault(pathName, null);
	}

	@Override
	public void add(Serializable pathname, Object resource) {
		// TODO Auto-generated method stub
		if (context == null) {
			context = new HashMap<>(8);
		}

		context.put(pathname, resource);
	}

	@Override
	public void remove(String pathName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(String pathName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@PostConstruct
	private void init() {
		logger.debug("ResourceContextImpl started in thread: " + Thread.currentThread().getId());
	}

	@PreDestroy
	private void destroy() {
		logger.debug("ResourceContextImpl destroyed in thread: " + Thread.currentThread().getId());
	}

}
