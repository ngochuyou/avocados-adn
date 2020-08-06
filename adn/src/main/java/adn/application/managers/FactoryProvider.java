/**
 * 
 */
package adn.application.managers;

import java.util.HashMap;
import java.util.Map;

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
import adn.factory.EMFactory;
import adn.factory.Factory;
import adn.model.Entity;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(4)
public class FactoryManager implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, Factory<? extends Entity, ? extends Model>> factoryMap;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());
		this.factoryMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(Factory.class));

		for (BeanDefinition def : scanner.findCandidateComponents(Constants.factoryPackage)) {
			try {
				Class<? extends Factory> factoryClass = (Class<? extends Factory>) Class
						.forName(def.getBeanClassName());
				EMFactory annotation = factoryClass.getDeclaredAnnotation(EMFactory.class);

				if (annotation == null) {
					throw new Exception("Can not find " + EMFactory.class.getName() + " on " + factoryClass.getName());
				}

				Class<? extends Entity> entityClass = annotation.entityClass();

				factoryMap.put(entityClass, context.getBean(factoryClass));
				logger.info("Assigning " + factoryClass.getName() + " for production of " + entityClass.getName()
						+ " and " + annotation.modelClass().getName());
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(context);
			}
		}

		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings({ "unchecked" })
	public <E extends Entity, M extends Model> Factory<E, M> getFactory(Class<E> clazz) {

		return (Factory<E, M>) factoryMap.get(clazz);
	}

}
