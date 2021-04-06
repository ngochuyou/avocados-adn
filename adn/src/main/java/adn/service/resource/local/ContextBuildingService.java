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
import java.util.stream.Collectors;

import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuildingService extends ServiceRegistry {

	public static ContextBuildingService createBuildingService() {
		return new BuildingService();
	}

	void register(Class<?> type, Service newService);

	static class BuildingService implements ContextBuildingService {

		private BuildingService() {}

		private final Map<Class<?>, Set<Service>> serviceMap = new HashMap<>();

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		@Override
		public ServiceRegistry getParentServiceRegistry() {
			// TODO Auto-generated method stub
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(Class<T> serviceRole) {
			// TODO Auto-generated method stub
			// @formatter:off
			Set<Class<?>> candidateKeys = serviceMap.keySet().stream()
					.filter(serviceClassKey -> serviceClassKey.isAssignableFrom(serviceRole))
					.collect(Collectors.toSet());
			long n = candidateKeys.size();
			
			Assert.isTrue(n != 0, "No service class of type: " + serviceRole + " were registered");
			Assert.isTrue(n == 1, "More than one service class of type: " + serviceRole + " were registered");
			
			Set<Service> candidateSet = serviceMap.get(candidateKeys.stream().findFirst().orElseThrow());
	
			candidateSet.stream().filter(service -> serviceRole.isAssignableFrom(service.getClass()));
			n = candidateSet.size();
			// @formatter:on
			if (n == 1) {
				return (T) candidateSet.stream().findFirst().orElseThrow();
			}

			throw n == 0 ? new NoSuchElementException("Could not find Service of type: " + serviceRole)
					: new IllegalArgumentException("More than one candidate of type: " + serviceRole + " were found.");
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
		}

		@Override
		public void register(Class<?> clazz, Service service) {
			// TODO Auto-generated method stub
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

	}

}
