/**
 * 
 */
package adn.model;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.ContextBuilder;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(0)
public class ModelManager implements ContextBuilder {

	private transient Logger logger = LoggerFactory.getLogger(this.getClass());

	private ModelInheritanceTree<adn.model.entities.Entity> entityTree;

	private ModelInheritanceTree<Model> modelTree;

	private Map<Class<? extends adn.model.entities.Entity>, Set<Class<? extends Model>>> relationMap;

	private Map<Class<? extends adn.model.entities.Entity>, Class<? extends Model>> defaultModelMap;

	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		initializeEntityTree();
		initializeModelTree();
		initializeRelationMap();
		initializeDefaultModelMap();
	}

	@SuppressWarnings("unchecked")
	private void initializeEntityTree() {
		this.entityTree = new ModelInheritanceTree<>(null, adn.model.entities.Entity.class, null);
		logger.info("[0]Initializing " + this.entityTree.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(adn.model.entities.Entity.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.entityPackage)) {
				Class<? extends adn.model.entities.Entity> clazz = (Class<? extends adn.model.entities.Entity>) Class
						.forName(beanDef.getBeanClassName());
				Stack<Class<?>> stack = reflector.getClassStack(clazz);

				while (!stack.isEmpty()) {
					this.entityTree.add((Class<adn.model.entities.Entity>) stack.pop());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(context);
		}
		this.entityTree.forEach(tree -> {
			logger.info(tree.getNode().getName() + " added to " + this.entityTree.getClass().getName()
					+ (tree.getParent() == null ? " as super root"
							: " with root " + tree.getParent().getNode().getName()));
		});
		// @formatter:on
		logger.info("[0]Finished initializing " + this.entityTree.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public void initializeModelTree() {
		this.modelTree = new ModelInheritanceTree<>(null, Model.class, null);
		logger.info("Initializing " + this.modelTree.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.modelPackage)) {
				Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(beanDef.getBeanClassName());
				Stack<Class<?>> stack = reflector.getClassStack(clazz);

				while (!stack.isEmpty()) {
					this.modelTree.add((Class<Model>) stack.pop());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(context);
		}
		this.modelTree.forEach(tree -> {
			logger.info(tree.getNode().getName() + " added to " + this.modelTree.getClass().getName()
					+ (tree.getParent() == null ? " as super root"
							: " with root " + tree.getParent().getNode().getName()));
		});
		// @formatter:on
		logger.info("Finished initializing " + this.entityTree.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private void initializeRelationMap() {
		this.relationMap = new HashMap<>();
		logger.info("Initializing ModelMap");

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		HashSet<Class<? extends Model>> models = new HashSet<>();
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));
		scanner.addIncludeFilter(new AnnotationTypeFilter(Genetized.class));
		scanner.findCandidateComponents(Constants.modelPackage).stream().map(bean -> {
			try {
				Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(bean.getBeanClassName());

				if (!reflector.isExtendedFrom(clazz, Model.class)) {
					throw new Exception(clazz.getName() + " is a Non-standard Model. A Model must be extended from "
							+ Entity.class);
				}

				models.add(clazz);

				return clazz;
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(context);

				return null;
			}
		}).forEach(clazz -> {
			try {
				Field[] fields = clazz.getDeclaredFields();

				for (Field f : fields) {
					if (reflector.isExtendedFrom(f.getType(), AbstractModel.class) && models.contains(clazz)
							&& this.entityTree.contains((Class<? extends Model>) f.getType())) {
						throw new Exception(clazz.getName() + " is a Non-standard Model. " + f.getType().getName()
								+ " was modelized into a Model. Use the modelized type instead");
					}

					if (reflector.isImplementedFrom(f.getType(), Collection.class)) {
						ParameterizedType type = (ParameterizedType) f.getGenericType();
						Class<?> clz = (Class<?>) type.getActualTypeArguments()[0];

						if (reflector.isExtendedFrom(clz, Model.class) && models.contains(clazz)
								&& this.entityTree.contains((Class<? extends Model>) clz)) {
							throw new Exception(clazz.getName() + " is a Non-standard Model. " + clz.getName()
									+ " was modelized into a Model. Use the modelized type instead on field: "
									+ f.getName());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(context);
			}
		});
		models.forEach(clazz -> {
			Genetized annotaion = clazz.getDeclaredAnnotation(Genetized.class);
			Class<? extends adn.model.entities.Entity> relatedClass = annotaion.entityGene();

			if (this.relationMap.get(relatedClass) == null) {
				this.relationMap.put(relatedClass, Set.of(clazz));
			} else {
				Set<Class<? extends Model>> set = this.relationMap.get(relatedClass).stream()
						.collect(Collectors.toSet());

				set.add(clazz);
				this.relationMap.put(relatedClass, set);
			}
		});
		this.relationMap.forEach((key, val) -> val.forEach(
				clazz -> logger.info("Putting " + key.getName() + " and " + clazz.getName() + " as a EM relation")));
		// @formatter:on
		logger.info("Finished initializing ModelMap");
	}

	@SuppressWarnings("unchecked")
	private void initializeDefaultModelMap() {
		this.defaultModelMap = new HashMap<>();
		logger.info("Initializing DefaultModelMap");

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));
		scanner.findCandidateComponents(Constants.modelPackage)
			.forEach(bean -> {
				try {
					Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(bean.getBeanClassName());
					Order order = clazz.getDeclaredAnnotation(Order.class);
	
					if (order == null || order.value() != Ordered.HIGHEST_PRECEDENCE) {
						return;
					}
	
					this.defaultModelMap.put(clazz.getDeclaredAnnotation(Genetized.class).entityGene(), clazz);
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(context);
				}
			});
		this.defaultModelMap.forEach((k, v) -> logger.info(v.getName() + " assigned to be default model of " + k.getName()));
		// @formatter:on
		logger.info("Finished initializing DefaultModelMap");
	}

	public ModelInheritanceTree<adn.model.entities.Entity> getEntityTree() {
		return entityTree;
	}

	public Logger getLogger() {
		return logger;
	}

	public ModelInheritanceTree<Model> getModelTree() {
		return modelTree;
	}

	public Map<Class<? extends adn.model.entities.Entity>, Set<Class<? extends Model>>> getRelationMap() {

		return relationMap;
	}

	public Class<? extends Model> getModelClass(Class<? extends adn.model.entities.Entity> entityClass) {

		return this.defaultModelMap.get(entityClass);
	}

}
