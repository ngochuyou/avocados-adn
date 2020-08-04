/**
 * 
 */
package adn.application.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.application.Constants;
import adn.model.Entity;
import adn.service.ApplicationGenericService;
import adn.service.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class ServiceManager implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, ApplicationGenericService<? extends Entity>> serviceMap;

	private ApplicationGenericService<?> defaultService = new ApplicationGenericService<Entity>() {
	};

	private ModelManager modelManager = context.getBean(ModelManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(ApplicationGenericService.class));
		serviceMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.servicePackage);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends ApplicationGenericService<?>> clazz = (Class<? extends ApplicationGenericService<?>>) Class
						.forName(beanDef.getBeanClassName());
				GenericService anno = clazz.getDeclaredAnnotation(GenericService.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.target();

				serviceMap.put(modelClass, context.getBean(clazz));
			}

			modelManager.getEntityTree().forEach(treeNode -> {
				if (serviceMap.get(treeNode.getNode()) == null) {
					serviceMap.put(treeNode.getNode(), defaultService);
				}

				logger.info("Assigning " + serviceMap.get(treeNode.getNode()).getClass().getName() + " for "
						+ treeNode.getNode().getName());
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(context);
		}

		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> ApplicationGenericService<T> getService(Class<T> clazz) {

		return (ApplicationGenericService<T>) this.serviceMap.get(clazz);
	}

}
