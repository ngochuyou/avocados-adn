/**
 * 
 */
package adn.application.managers;

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
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.application.Constants;
import adn.model.EntityTree;
import adn.model.Model;
import adn.model.Modelized;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(0)
public class ModelManager implements ApplicationManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private EntityTree entityTree;

	private Map<Class<? extends Model>, Set<Class<? extends Model>>> modelMap;

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		this.initializeEntityTree();
		this.initializeModelMap();
	}

	@SuppressWarnings("unchecked")
	private void initializeEntityTree() {
		this.entityTree = new EntityTree(null, Model.class, null);
		logger.info("Initializing " + this.entityTree.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));
		scanner.findCandidateComponents(Constants.entityPackage)
			.forEach(bean -> {
				try {
					Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(bean.getBeanClassName()); 
					Stack<Class<? extends Model>> stack = reflector.getModelClassStack(clazz);
					
					while (!stack.isEmpty()) {
						this.entityTree.add(stack.pop());
					}
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(context);
				}
			});
		this.entityTree.forEach(tree -> {
			logger.info(tree.getNode().getName() + " added to " +
					this.entityTree.getClass().getName() +
						(tree.getParent() == null ? " as super root"
								: " with root " + tree.getParent().getNode().getName()));
		});
		// @formatter:on
		logger.info("Finished initializing " + this.entityTree.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private void initializeModelMap() {
		this.modelMap = new HashMap<>();
		logger.info("Initializing ModelMap");

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		HashSet<Class<? extends Model>> models = new HashSet<>();
		// @formatter:off
		scanner.addIncludeFilter(new AnnotationTypeFilter(Modelized.class));
		scanner.findCandidateComponents(Constants.modelPackage)
			.stream()
			.map(bean -> {
				try {
					Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(bean.getBeanClassName());
					
					if (!reflector.isExtendedFrom(clazz, Model.class)) {
						throw new Exception(clazz.getName() + " is a Non-standard Model. A Model must be extended from " + Model.class);
					}
					
					models.add(clazz);
					
					return clazz;
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(context);
					
					return null;
				}
			})
			.forEach(clazz -> {
				try {
					Field[] fields = clazz.getDeclaredFields();
					
					for (Field f: fields) {
						if (reflector.isExtendedFrom(f.getType(), Model.class) && models.contains(clazz) && this.entityTree.contains((Class<? extends Model>) f.getType())) {
							throw new Exception(clazz.getName() + " is a Non-standard Model. " + f.getType().getName() + " was modelized into a Model. Use the modelized type instead");
						}
						
						if (reflector.isImplementedFrom(f.getType(), Collection.class)) {
							ParameterizedType type = (ParameterizedType) f.getGenericType();
					        Class<?> clz = (Class<?>) type.getActualTypeArguments()[0];
					        
					        if (reflector.isExtendedFrom(clz, Model.class) && models.contains(clazz) && this.entityTree.contains((Class<? extends Model>) clz)) {
								throw new Exception(clazz.getName() + " is a Non-standard Model. " + clz.getName() + " was modelized into a Model. Use the modelized type instead on field: " + f.getName());
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(context);
				}
			});
		models.forEach(clazz -> {
			Modelized annotaion = clazz.getDeclaredAnnotation(Modelized.class);
			Class<? extends Model> relatedClass = annotaion.relation();

			if (this.modelMap.get(relatedClass) == null) {
				this.modelMap.put(relatedClass, Set.of(clazz));
			} else {
				Set<Class<? extends Model>> set = this.modelMap.get(relatedClass)
						.stream()
						.collect(Collectors.toSet());
				
				set.add(clazz);
				this.modelMap.put(relatedClass, set);
			}
		});
		this.modelMap.forEach((key, val) -> val.forEach(clazz -> logger.info("Putting " + key.getName() + " and " + clazz.getName() + " as a EM relation")));
		// @formatter:on
		logger.info("Finished initializing ModelMap");
	}

	public EntityTree getEntityTree() {
		return entityTree;
	}

	public void setEntityTree(EntityTree entityTree) {
		this.entityTree = entityTree;
	}

	public Map<Class<? extends Model>, Set<Class<? extends Model>>> getModelMap() {
		return modelMap;
	}

	public void setModelMap(Map<Class<? extends Model>, Set<Class<? extends Model>>> modelMap) {
		this.modelMap = modelMap;
	}

}
