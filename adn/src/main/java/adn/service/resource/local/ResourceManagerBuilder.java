/**
 * 
 */
package adn.service.resource.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(6)
public class ResourceManagerBuilder implements ContextBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContextBuildingService contextBuildingService;

	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Building " + this.getClass());
		// create building service
		contextBuildingService = new ContextBuildingService();
		// create Metadata
		final Metadata metadata = new ClassPathScanningMetadata();
		
		contextBuildingService.register(Metadata.class, metadata);
		// register naming strategy
		contextBuildingService.register(NamingStrategy.class, NamingStrategy.DEFAULT_NAMING_STRATEGY);
		
		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) ContextProvider
				.getApplicationContext()).getBeanFactory();
		ResourceManager resourceManager = build();

		beanFactory.registerSingleton(resourceManager.getClass().getName(), resourceManager);
		beanFactory.registerAlias(resourceManager.getClass().getName(), ResourceManager.class.getName());

		logger.info(getLoggingPrefix(this) + "Finished building" + this.getClass());
	}

	private ResourceManager build() throws IllegalAccessException, NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		return new GlobalResourceManager(contextBuildingService);
	}

}
