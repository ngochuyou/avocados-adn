/**
 * 
 */
package adn.application.managers;

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

import adn.application.ApplicationManager;
import adn.application.Constants;
import adn.model.Entity;
import adn.model.specification.GenericSpecification;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(2)
public class SpecificationFactory implements ApplicationManager {

	private Map<Class<? extends Entity>, Specification<?>> specificationMap;

	private Logger logger = LoggerFactory.getLogger(SpecificationFactory.class);

	private Specification<?> defaultSpecification = new Specification<Entity>() {
	};

	@Autowired
	private ModelManager modelManager;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());
		this.specificationMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Specification.class));
		scanner.findCandidateComponents(Constants.specificationPackage)
			.stream()
			.map(bean -> {
				try {
					return (Class<? extends Specification<?>>) Class.forName(bean.getBeanClassName());
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(context);
					
					return null;
				}
			})
			.forEach(clazz -> {
				try {
					GenericSpecification anno = clazz.getDeclaredAnnotation(GenericSpecification.class);
					
					if (anno == null) {
						throw new Exception(GenericSpecification.class.getName() + " is not found on " + clazz.getName());
					}
					
					Class<? extends Entity> modelClass = anno.target();
					
					this.specificationMap.put(modelClass, context.getBean(clazz));
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(context);
				}
			});
		this.modelManager
			.getEntityTree()
			.forEach(tree -> {
				if (tree.getParent() == null) {
					return;
				}
				
				Specification<?> parentSpec = this.specificationMap.get(tree.getParent().getNode());
				Specification<?> childrenSpec = this.specificationMap.get(tree.getNode());
				Specification<?> spec = null;
				
				if (parentSpec != null && childrenSpec != null) {
					spec = parentSpec.and(childrenSpec);
				} else {
					spec = parentSpec != null ? parentSpec : childrenSpec;
				}

				this.specificationMap.put(tree.getNode(), spec == null ? defaultSpecification : spec);
			});
		this.specificationMap.forEach((k, v) -> logger.info(v.getName() + " is applied on " + k.getName()));
		// @formatter:on
		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> Specification<T> getSpecification(Class<T> clazz) {

		return (Specification<T>) this.specificationMap.get(clazz);
	}

}
