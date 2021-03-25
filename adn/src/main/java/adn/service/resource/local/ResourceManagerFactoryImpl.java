/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceManagerFactoryImpl implements ResourceManagerFactory {

	private final Map<String, ResourceDescriptor<?>> descriptorsByName = new HashMap<>();

	private final Set<String> managedModels;

	private final Metadata metadata;

	private final NamingStrategy resourceNamingStrategy;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ResourceManager resourceManager = ContextProvider.getApplicationContext()
			.getBean(ResourceManager.class);

	/**
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * 
	 */
	public ResourceManagerFactoryImpl(final ContextBuildingService serviceContext)
			throws IllegalAccessException, NoSuchMethodException, SecurityException {
		// TODO Auto-generated constructor stub
		Assert.notNull(serviceContext, "ContextBuildingService cannot be null");

		metadata = serviceContext.getService(Metadata.class);
		resourceNamingStrategy = serviceContext.getService(NamingStrategy.class);

		Set<Class<?>> modelClassSet = metadata.getModelClassSet();

		managedModels = modelClassSet.stream().map(clazz -> resourceNamingStrategy.getName(clazz))
				.collect(Collectors.toSet());
		// create descriptors
		for (Class<?> modelClass : modelClassSet) {
			ResourceDescriptor<?> descriptor = createDescriptor(modelClass);

			descriptorsByName.put(resourceNamingStrategy.getName(modelClass), descriptor);
			// @formatter:off
			logger.debug(String.format("\nCreated descriptor for type: %s with name: %s\n"
					+ "\t-idGetter: %s\n"
					+ "\t-idSetter: %s\n"
					+ "\t-isIdentifierAutoGenerated: %s\n"
					+ "\t-identifierValueGenerator: %s",
					modelClass.getName(), resourceNamingStrategy.getName(modelClass),
					descriptor.getIdentifierGetter().getMethodName(),
					descriptor.getIdentifierSetter().getMethodName(),
					Boolean.valueOf(descriptor.isIdentifierAutoGenerated()),
					(descriptor.isIdentifierAutoGenerated() ? 
							descriptor.getIdentifierValueGeneration().getValueGenerator().getClass().getName() :
								"NONE" )));
			// @formatter:on
		}
	}

	private ResourceDescriptor<?> createDescriptor(Class<?> clazz) throws NoSuchMethodException, SecurityException {
		return new ResourceDescriptorImpl<>(clazz, this);
	}

	@Override
	public boolean isLocked(Serializable identifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setLocked(Serializable identifier, boolean isLocked) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> ResourceDescriptor<T> getResourceDescriptor(Class<T> resourceClass) {
		// TODO Auto-generated method stub
		return getResourceDescriptor(resourceNamingStrategy.getName(resourceClass));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ResourceDescriptor<T> getResourceDescriptor(String resourceName) {
		// TODO Auto-generated method stub
		if (!managedModels.contains(resourceName)) {
			return null;
		}

		return (ResourceDescriptor<T>) descriptorsByName.get(resourceName);
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
		return resourceManager;
	}

}
