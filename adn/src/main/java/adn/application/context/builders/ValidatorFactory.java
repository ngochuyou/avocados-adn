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
import adn.model.entities.validator.Validator;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ValidatorFactory implements ContextBuilder {

	private Map<Class<? extends Entity>, Validator<?>> validatorMap;
	private Validator<Entity> defaultValidator = new Validator<>() {

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
		Logger logger = LoggerFactory.getLogger(ValidatorFactory.class);

		logger.info("Building " + this.getClass().getName());
		this.validatorMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Validator.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.GENERIC_SPECIFICATION_PACKAGE)) {
				Class<? extends Validator<?>> clazz = (Class<? extends Validator<?>>) Class.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);
				
				if (!Entity.class.isAssignableFrom(anno.entityGene())) {
					continue;
				}
				
				validatorMap.put((Class<? extends Entity>) anno.entityGene(),
						(Validator<?>) ContextProvider.getApplicationContext()
							.getBean(TypeHelper.getComponentName(clazz)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		modelManager.getEntityTree()
			.forEach(branch -> {
				if (validatorMap.get(branch.getNode()) == null) {
					if (branch.getParent() == null) {
						validatorMap.put((Class<? extends Entity>) branch.getNode(), defaultValidator);
						return;
					}
					
					Validator<?> parentSpec = this.validatorMap.get(branch.getParent().getNode());
					
					validatorMap.put((Class<? extends Entity>) branch.getNode(), parentSpec != null ? parentSpec : defaultValidator);
				}
			});
		validatorMap.forEach((k, v) -> logger.debug(String.format("Registered one %s of type [%s] for [%s] ", Validator.class.getSimpleName(), v.getClass().getName(), k.getName())));
		// @formatter:on
		logger.info("Finished building" + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> Validator<T> getValidator(Class<T> clazz) {
		return (Validator<T>) this.validatorMap.get(clazz);
	}

}
