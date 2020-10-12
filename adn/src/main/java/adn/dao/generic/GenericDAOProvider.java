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
import adn.model.Genetized;
import adn.model.ModelManager;
import adn.model.entities.Entity;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class GenericDAOProvider implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, GenericDAO<? extends Entity>> genericDAOMap;

	private GenericDAO<?> defaultGDAO = new GenericDAO<Entity>() {};

	private ModelManager modelManager = context.getBean(ModelManager.class);

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(GenericDAO.class));
		genericDAOMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.genericDAOPackage);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends GenericDAO<?>> clazz = (Class<? extends GenericDAO<?>>) Class
						.forName(beanDef.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.entityGene();

				genericDAOMap.put(modelClass,
						(GenericDAO<? extends Entity>) context.getBean(Strings.toCamel(clazz.getSimpleName(), null)));
			}

			modelManager.getEntityTree().forEach(node -> {
				if (this.genericDAOMap.get(node.getNode()) == null) {
					GenericDAO<?> parentGDAO = genericDAOMap.get(node.getParent().getNode());

					genericDAOMap.put(node.getNode(), parentGDAO == null ? defaultGDAO : parentGDAO);
				}
			});

			modelManager.getEntityTree().forEach(treeNode -> {
				logger.info("Assigning " + genericDAOMap.get(treeNode.getNode()).getClass().getName() + " for "
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
		logger.debug("Providing GerericDAO for " + clazz.getName());

		return (GenericDAO<T>) this.genericDAOMap.get(clazz);
	}

}
