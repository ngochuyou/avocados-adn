/**
 * 
 */
package adn.model.specification;

import java.util.HashMap;
import java.util.Map;

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
import adn.helpers.TypeHelper;
import adn.model.Generic;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(2)
public class SpecificationFactory implements ContextBuilder {

	private Map<Class<? extends Entity>, Specification<?>> specificationMap;

	private Logger logger = LoggerFactory.getLogger(SpecificationFactory.class);

	private Specification<?> defaultSpecification = new Specification<>() {};

	@Autowired
	private ModelContextProvider modelManager;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());
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
		specificationMap.forEach((k, v) -> logger.info(String.format("Registered one %s of type [%s] for [%s] ", Specification.class.getSimpleName(), v.getClass().getName(), k.getName())));
		// @formatter:on
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> Specification<T> getSpecification(Class<T> clazz) {
		return (Specification<T>) this.specificationMap.get(clazz);
	}

}
