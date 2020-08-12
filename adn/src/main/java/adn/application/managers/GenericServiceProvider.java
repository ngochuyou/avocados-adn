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
import adn.model.Genetized;
import adn.service.ApplicationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class GenericServiceProvider implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, ApplicationService<? extends Entity>> serviceMap;

	private ApplicationService<?> defaultService = new ApplicationService<Entity>() {
	};

	private ModelManager modelManager = context.getBean(ModelManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(ApplicationService.class));
		serviceMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.servicePackage);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends ApplicationService<?>> clazz = (Class<? extends ApplicationService<?>>) Class
						.forName(beanDef.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.gene();

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
	public <T extends Entity> ApplicationService<T> getService(Class<T> clazz) {

		return (ApplicationService<T>) this.serviceMap.get(clazz);
	}

}
