/**
 * 
 */
package adn.service.resource.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;
import adn.service.resource.metamodel.ClassPathScanningSource;
import adn.service.resource.metamodel.Metadata;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(6)
public class ResourceManagerFactoryBuilder implements ContextBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ContextBuildingService contextBuildingService;

	@Autowired
	private LocalResourceStorage localStorage;

	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Building " + this.getClass());
		// create building service
		contextBuildingService = ContextBuildingService.createBuildingService();
		// register naming-strategy
		contextBuildingService.register(NamingStrategy.class, NamingStrategy.DEFAULT_NAMING_STRATEGY);
		contextBuildingService.register(LocalResourceStorage.class, localStorage);
		// create Metadata
		final Metadata metadata = new ClassPathScanningSource(contextBuildingService);
		
		metadata.process();
		contextBuildingService.register(Metadata.class, metadata);
		// inject ResourceManager bean into ApplicationContext
		// usages of this bean should be obtained via
		// ContextProvider.getApplicationContext().getBean(ResourceManager.class.getName());
		// or
		// ContextProvider.getApplicationContext().getBean([Explicit bean name]);
		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) ContextProvider
				.getApplicationContext()).getBeanFactory();
		ResourceManagerFactory resourceManager = build();

		beanFactory.registerSingleton(resourceManager.getClass().getName(), resourceManager);
		beanFactory.registerAlias(resourceManager.getClass().getName(), ResourceManagerFactory.class.getName());

		logger.info(getLoggingPrefix(this) + "Finished building " + this.getClass());

//		EntityPersister cp = factory.unwrap(SessionFactoryImplementor.class).getServiceRegistry()
//				.getService(PersisterFactory.class);
	}

	private ResourceManagerFactory build()
			throws IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		// TODO Auto-generated method stub
		return new ResourceManagerFactoryImpl(contextBuildingService);
	}

}
