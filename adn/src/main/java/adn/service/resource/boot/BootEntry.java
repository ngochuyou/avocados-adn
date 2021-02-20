/**
 * 
 */
package adn.service.resource.boot;

import javax.persistence.SharedCacheMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import adn.application.context.ContextProvider;
import adn.service.resource.metamodel.DefaultNamingStrategy;
import adn.service.resource.metamodel.NamingStrategyDelegate;
import adn.service.resource.tuple.ResourceTuplizerFactory;

/**
 * Boot entry point
 * 
 * @author Ngoc Huy
 *
 */
public class BootEntry {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final MetadataSources metadataSources;

	private final NamingStrategyDelegate namingStrategy;

	private final ResourceTuplizerFactory resourceTuplizerFactory;
	private SharedCacheMode cacheMode;

	/**
	 * 
	 */
	public BootEntry() {
		// TODO Auto-generated constructor stub
		final ApplicationContext context = ContextProvider.getApplicationContext();

		metadataSources = context.getBean(MetadataSources.class);
		resourceTuplizerFactory = context.getBean(ResourceTuplizerFactory.class);
		cacheMode = SharedCacheMode.ENABLE_SELECTIVE; // by default
		namingStrategy = context.getBean(DefaultNamingStrategy.class); // simple naming strategy
	}

	public LocalStorageSessionFactory buildSessionFactory() {
		logger.info("Building an instance of " + this.getClass().getName());

		return null;
	}

	/**
	 * @param cacheMode the cacheMode to set
	 */
	public void setCacheMode(SharedCacheMode cacheMode) {
		this.cacheMode = cacheMode;
	}

}
