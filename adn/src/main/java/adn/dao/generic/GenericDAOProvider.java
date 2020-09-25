/**
 * 
 */
package adn.dao.generic;

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

import adn.application.Constants;
import adn.application.context.ContextBuilder;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.ModelManager;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class GenericDAOProvider implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, GenericDAO<? extends Entity>> serviceMap;

	private GenericDAO<?> defaultService = new GenericDAO<Entity>() {};

	private ModelManager modelManager = context.getBean(ModelManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(GenericDAO.class));
		serviceMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.genericDAOPackage);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends GenericDAO<?>> clazz = (Class<? extends GenericDAO<?>>) Class
						.forName(beanDef.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.entityGene();

				serviceMap.put(modelClass, context.getBean(clazz));
			}

			modelManager.getEntityTree().forEach(treeNode -> {
				if (treeNode.getParent() == null) {
					return;
				}

				GenericDAO<?> compositeService;
				GenericDAO<?> parentService = serviceMap.get(treeNode.getParent().getNode());
				GenericDAO<?> childService = serviceMap.get(treeNode.getNode());

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
	public <T extends Entity> GenericDAO<T> getService(Class<T> clazz) {

		return (GenericDAO<T>) this.serviceMap.get(clazz);
	}

}
