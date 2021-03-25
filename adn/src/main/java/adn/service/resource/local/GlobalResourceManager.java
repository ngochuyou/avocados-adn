/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class GlobalResourceManager implements ResourceManager {

//	private volatile Map<String, Boolean> lockStateContext = Collections.synchronizedMap(new HashMap<>());

//	private final Map<Serializable, ResourcePersistenceContext> persistenceContextMap = new HashMap<>(1000, 0.75f);

	private final Map<String, ResourceTuplizer<?>> tuplizersByName = new HashMap<>();

	private final Set<String> managedModels;

	private final Metadata metadata;

	private final NamingStrategy resourceNamingStrategy;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * 
	 */
	public GlobalResourceManager(final ContextBuildingService serviceContext)
			throws IllegalAccessException, NoSuchMethodException, SecurityException {
		// TODO Auto-generated constructor stub
		Assert.notNull(serviceContext, "ContextBuildingService cannot be null");

		metadata = serviceContext.getService(Metadata.class);
		resourceNamingStrategy = serviceContext.getService(NamingStrategy.class);

		Set<Class<?>> modelClassSet = metadata.getModelClassSet();

		managedModels = modelClassSet.stream().map(clazz -> resourceNamingStrategy.getName(clazz))
				.collect(Collectors.toSet());
		// create Tuplizers
		for (Class<?> modelClass : modelClassSet) {
			ResourceTuplizer<?> tuplizer = createTuplizer(modelClass);

			tuplizersByName.put(resourceNamingStrategy.getName(modelClass), tuplizer);
			// @formatter:off
			logger.debug(String.format("\nCreated tuplizer for type: %s with name: %s\n"
					+ "\t-idGetter: %s\n"
					+ "\t-idSetter: %s",
					modelClass,
					resourceNamingStrategy.getName(modelClass),
					tuplizer.getIdGetter().getMethodName(),
					tuplizer.getIdSetter().getMethodName()));
			// @formatter:on
		}
	}

	private ResourceTuplizer<?> createTuplizer(Class<?> clazz) throws NoSuchMethodException, SecurityException {
		return new ResourceTuplizerImpl<>(clazz, this);
	}

	@Override
	public ResourcePersistenceContext getContext(Serializable id)
			throws IllegalStateException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return (ResourcePersistenceContext) TransactionSynchronizationManager.getResource(id);
	}

	@Override
	public ResourcePersistenceContext openContext() {
		// TODO Auto-generated method stub
		String uuid = UUID.randomUUID().toString();
		ResourcePersistenceContext context;
		TransactionSynchronizationManager.bindResource(uuid, context = new ResourceContextImpl(this));

		return context;
	}

	@Override
	public void doContextClose(ResourcePersistenceContext context) {
		// TODO Auto-generated method stub
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
	public <T> ResourceTuplizer<T> getTuplizer(Class<T> resourceClass) {
		// TODO Auto-generated method stub
		return getTuplizer(resourceNamingStrategy.getName(resourceClass));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ResourceTuplizer<T> getTuplizer(String resourceName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		if (!managedModels.contains(resourceName)) {
			throw new IllegalArgumentException("Could not obtain tuplizer for resource of name: " + resourceName
					+ ", provided resource name is not a managed type");
		}

		return (ResourceTuplizer<T>) tuplizersByName.get(resourceName);
	}

}
