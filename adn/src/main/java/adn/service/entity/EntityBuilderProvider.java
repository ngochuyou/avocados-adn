/**
 * 
 */
package adn.service.entity;

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
import adn.dao.EntityBuilder;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.ModelsDescriptor;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(3)
public class EntityBuilderProvider implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<Class<? extends Entity>, EntityBuilder<? extends Entity>> builderMap;

	private EntityBuilder<?> defaultBuilder = new EntityBuilder<Entity>() {};

	@Autowired
	private ModelsDescriptor modelDescriptor;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EntityBuilder.class));
		builderMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.ENTITY_BUILDER_PACKAGE);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends EntityBuilder<?>> clazz = (Class<? extends EntityBuilder<?>>) Class
						.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				if (anno == null)
					continue;

				Class<? extends Entity> modelClass = anno.entityGene();

				builderMap.put(modelClass, (EntityBuilder<? extends Entity>) ContextProvider.getApplicationContext()
						.getBean(StringHelper.toCamel(clazz.getSimpleName(), null)));
			}

			modelDescriptor.getEntityTree().forEach(node -> {
				if (this.builderMap.get(node.getNode()) == null) {
					if (!builderMap.containsKey(node.getNode())) {
						builderMap.put(node.getNode(), defaultBuilder);
					}
				}
			});

			modelDescriptor.getEntityTree().forEach(node -> {
				logger.info(String.format("[%s] -> [%s]", builderMap.get(node.getNode()).getClass(),
						node.getNode().getName()));
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> EntityBuilder<T> getBuilder(Class<T> clazz) {
		return (EntityBuilder<T>) this.builderMap.get(clazz);
	}

}
