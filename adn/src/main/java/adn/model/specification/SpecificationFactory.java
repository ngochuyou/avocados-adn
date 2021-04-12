/**
 * 
 */
package adn.model.specification;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.Genetized;
import adn.model.ModelManager;
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

	private Specification<?> defaultSpecification = new Specification<Entity>() {};

	@Autowired
	private ModelManager modelManager;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());
		this.specificationMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Specification.class));
		scanner.findCandidateComponents(Constants.GENERIC_SPECIFICATION_PACKAGE)
			.forEach(bean -> {
				try {
					Class<? extends Specification<?>> clazz = (Class<? extends Specification<?>>) Class.forName(bean.getBeanClassName());
					Genetized anno = clazz.getDeclaredAnnotation(Genetized.class);
					
					if (anno == null) {
						throw new Exception(Genetized.class.getName() + " not found on" + bean.getBeanClassName());
					}

					specificationMap.put(anno.entityGene(), (Specification<?>) ContextProvider.getApplicationContext().getBean(TypeHelper.getComponentName(clazz)));
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(ContextProvider.getApplicationContext());
				}
			});
		this.modelManager.getEntityTree()
			.forEach(node -> {
				if (this.specificationMap.get(node.getNode()) == null) {
					Specification<?> parentSpec = this.specificationMap.get(node.getParent().getNode());
					
					this.specificationMap.put(node.getNode(), parentSpec != null ? parentSpec : defaultSpecification);
				}
			});
		this.specificationMap.forEach((k, v) -> logger.info(v.getName() + " is applied on " + k.getName()));
		// @formatter:on
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> Specification<T> getSpecification(Class<T> clazz) {
		logger.debug("Providing Specification for " + clazz.getName());

		return (Specification<T>) this.specificationMap.get(clazz);
	}

}
