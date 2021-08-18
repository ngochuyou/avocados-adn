/**
 * 
 */
package adn.application.context.builders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.TypeHelper;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.service.entity.builder.EntityBuilder;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class EntityBuilderProvider implements ContextBuilder {

	private Map<Class<? extends Entity>, EntityBuilder<? extends Entity>> builderMap;
	private static final EntityBuilder<Entity> DEFAULT_BUILDER = new EntityBuilder<Entity>() {

		@Override
		public <E extends Entity> E buildUpdate(Serializable id, E entity, E persistence) {
			return persistence;
		}

		@Override
		public <E extends Entity> E buildInsertion(Serializable id, E entity) {
			return entity;
		}

	};

	@Autowired
	private ModelContextProvider modelDescriptor;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EntityBuilder.class));
		builderMap = new HashMap<>();

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.ENTITY_BUILDER_PACKAGE);

		try {
			for (BeanDefinition beanDef : beanDefs) {
				Class<? extends EntityBuilder<?>> clazz = (Class<? extends EntityBuilder<?>>) Class
						.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				if (!Entity.class.isAssignableFrom(anno.entityGene()))
					continue;

				Class<? extends Entity> entityClass = (Class<? extends Entity>) anno.entityGene();

				builderMap.put(entityClass, (EntityBuilder<? extends Entity>) ContextProvider.getApplicationContext()
						.getBean(TypeHelper.getComponentName(clazz)));
			}

			modelDescriptor.getEntityTree().forEach(branch -> {
				if (builderMap.get(branch.getNode()) == null) {
					if (branch.getParent() == null) {
						builderMap.put((Class<? extends Entity>) branch.getNode(), DEFAULT_BUILDER);
						return;
					}

					if (!builderMap.containsKey(branch.getParent().getNode())) {
						builderMap.put((Class<? extends Entity>) branch.getNode(), DEFAULT_BUILDER);
						return;
					}

					builderMap.put((Class<? extends Entity>) branch.getNode(),
							builderMap.get(branch.getParent().getNode()));
				}
			});

			modelDescriptor.getEntityTree().forEach(node -> {
				logger.debug(String.format("[%s] -> [%s]", builderMap.get(node.getNode()).getClass(),
						node.getNode().getName()));
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		logger.info("Finished building " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> EntityBuilder<T> getBuilder(Class<T> clazz) {
		return (EntityBuilder<T>) this.builderMap.get(clazz);
	}

}
