/**
 * 
 */
package adn.application.context.builders;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
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
import adn.dao.generic.Result;
import adn.helpers.TypeHelper;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.entities.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class SpecificationFactory implements ContextBuilder {

	private Map<Class<? extends Entity>, Specification<?>> specificationMap;
	private Specification<Entity> defaultSpecification = new Specification<>() {

		@Override
		public Result<Entity> isSatisfiedBy(Session session, Entity instance) {
			return Result.success(instance);
		}

		@Override
		public Result<Entity> isSatisfiedBy(Session session, Serializable id, Entity instance) {
			return Result.success(instance);
		}

	};

	@Autowired
	private ModelContextProvider modelManager;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		Logger logger = LoggerFactory.getLogger(SpecificationFactory.class);

		logger.info("Building " + this.getClass().getName());
		this.specificationMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Specification.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.GENERIC_SPECIFICATION_PACKAGE)) {
				Class<? extends Specification<?>> clazz = (Class<? extends Specification<?>>) Class.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);
				
				if (!Entity.class.isAssignableFrom(anno.entityGene())) {
					continue;
				}
				
				specificationMap.put((Class<? extends Entity>) anno.entityGene(),
						(Specification<?>) ContextProvider.getApplicationContext()
							.getBean(TypeHelper.getComponentName(clazz)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		modelManager.getEntityTree()
			.forEach(branch -> {
				if (specificationMap.get(branch.getNode()) == null) {
					if (branch.getParent() == null) {
						specificationMap.put((Class<? extends Entity>) branch.getNode(), defaultSpecification);
						return;
					}
					
					Specification<?> parentSpec = this.specificationMap.get(branch.getParent().getNode());
					
					specificationMap.put((Class<? extends Entity>) branch.getNode(), parentSpec != null ? parentSpec : defaultSpecification);
				}
			});
		specificationMap.forEach((k, v) -> logger.debug(String.format("Registered one %s of type [%s] for [%s] ", Specification.class.getSimpleName(), v.getClass().getName(), k.getName())));
		// @formatter:on
		logger.info("Finished building" + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> Specification<T> getSpecification(Class<T> clazz) {
		return (Specification<T>) this.specificationMap.get(clazz);
	}

}
