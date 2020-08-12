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
import adn.service.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class GenericServiceProvider implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, GenericService<? extends Entity>> serviceMap;

	private GenericService<?> defaultService = new GenericService<Entity>() {};

	private ModelManager modelManager = context.getBean(ModelManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(GenericService.class));
		serviceMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.servicePackage);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends GenericService<?>> clazz = (Class<? extends GenericService<?>>) Class
						.forName(beanDef.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.gene();

				serviceMap.put(modelClass, context.getBean(clazz));
			}

			modelManager.getEntityTree().forEach(treeNode -> {
				if (treeNode.getParent() == null) {
					return;
				}

				GenericService<?> compositeService;
				GenericService<?> parentService = serviceMap.get(treeNode.getParent().getNode());
				GenericService<?> childService = serviceMap.get(treeNode.getNode());

				if (parentService != null && childService != null) {
					compositeService = parentService.and(childService);
				} else {
					compositeService = parentService != null ? parentService : childService;
				}

				serviceMap.put(treeNode.getNode(), compositeService == null ? defaultService : compositeService);
			});

			modelManager.getEntityTree().forEach(treeNode -> {
				logger.info("Assigning " + serviceMap.get(treeNode.getNode()).getName() + " for "
						+ treeNode.getNode().getName());
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(context);
		}

		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> GenericService<T> getService(Class<T> clazz) {

		return (GenericService<T>) this.serviceMap.get(clazz);
	}

}
