/**
 * 
 */
package adn.dao.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.ModelManager;
import adn.model.entities.Entity;
import adn.utilities.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class GenericDAOProvider implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, GenericDAO<? extends Entity>> genericDAOMap;

	private GenericDAO<?> defaultGenericDAO = new GenericDAO<Entity>() {};

	@Autowired
	private ModelManager modelManager;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(GenericDAO.class));
		genericDAOMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.GENERIC_DAO_PACKAGE);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends GenericDAO<?>> clazz = (Class<? extends GenericDAO<?>>) Class
						.forName(beanDef.getBeanClassName());
				Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.entityGene();

				genericDAOMap.put(modelClass, (GenericDAO<? extends Entity>) ContextProvider.getApplicationContext()
						.getBean(StringHelper.toCamel(clazz.getSimpleName(), null)));
			}

			modelManager.getEntityTree().forEach(node -> {
				if (this.genericDAOMap.get(node.getNode()) == null) {
					GenericDAO<?> parentGDAO = genericDAOMap.get(node.getParent().getNode());

					genericDAOMap.put(node.getNode(), parentGDAO == null ? defaultGenericDAO : parentGDAO);
				}
			});

			modelManager.getEntityTree().forEach(node -> {
				logger.info("Assigning " + genericDAOMap.get(node.getNode()).getClass().getName() + " for "
						+ node.getNode().getName());
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> GenericDAO<T> getService(Class<T> clazz) {
		logger.debug("Providing GerericDAO for " + clazz.getName());

		return (GenericDAO<T>) this.genericDAOMap.get(clazz);
	}

}
