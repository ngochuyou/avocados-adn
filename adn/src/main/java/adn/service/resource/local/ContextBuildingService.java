/**
 * 
 */
package adn.service.resource.local;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class ContextBuildingService {

	private final Map<Class<?>, Set<Service>> serviceMap = new HashMap<>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void register(Class<?> clazz, Service service) {
		if (serviceMap.containsKey(clazz)) {
			logger.debug("Registering new service with type collision: " + clazz);
			serviceMap.compute(clazz, (key, oldSet) -> {
				HashSet<Service> newSet = new HashSet<>();

				newSet.addAll(oldSet);
				newSet.add(service);

				return newSet;
			});
			return;
		}

		logger.debug("Registering new service: " + clazz);
		serviceMap.put(clazz, new HashSet<>(Arrays.asList(service)));
	}

	@SuppressWarnings("unchecked")
	public <T extends Service> T getService(Class<?> serviceClass) throws IllegalAccessException {
		// @formatter:off
		Class<?>[] candidateKeys = serviceMap.keySet().stream()
				.filter(serviceClassKey -> serviceClassKey.isAssignableFrom(serviceClass))
				.toArray(Class<?>[]::new);
		
		Assert.isTrue(candidateKeys.length != 0, "No service class of type: " + serviceClass + " were registered");
		Assert.isTrue(candidateKeys.length == 1, "More than one service class of type: " + serviceClass + " were registered");
		
		Set<Service> serviceSet = serviceMap.get(candidateKeys[0]);

		candidateKeys = serviceSet.stream()
				.filter(serviceClassKey -> serviceClassKey.getClass().isAssignableFrom(serviceClass))
				.toArray(Class<?>[]::new);
		// @formatter:on
		if (serviceSet.size() == 1) {
			return (T) serviceSet.stream().findFirst().orElseThrow();
		}

		if (serviceSet.isEmpty()) {
			throw new NoSuchElementException("Could not find Service of type: " + serviceClass);
		}

		throw new IllegalArgumentException("More than one candidate of type: " + serviceClass + " were found.");
	}

}
