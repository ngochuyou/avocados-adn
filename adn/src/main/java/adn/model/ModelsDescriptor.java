/**
 * 
 */
package adn.model;

import java.io.Serializable;
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

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tuple.entity.AbstractEntityTuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(0)
public class ModelsDescriptor implements ContextBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ModelInheritanceTree<adn.model.entities.Entity> entityTree;

	private ModelInheritanceTree<Model> modelTree;

	private Map<Class<? extends adn.model.entities.Entity>, Set<Class<? extends AbstractModel>>> relationMap;

	private Map<Class<? extends adn.model.entities.Entity>, Class<? extends AbstractModel>> defaultModelMap;

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		initializeEntityTree();
		logger.info("---------------------------------------");
		initializeModelTree();
		logger.info("---------------------------------------");
		initializeRelationMap();
		logger.info("---------------------------------------");
		initializeDefaultModelMap();
	}

	@SuppressWarnings("unchecked")
	private void initializeEntityTree() {
		this.entityTree = new ModelInheritanceTree<>(null, adn.model.entities.Entity.class, null);

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		scanner.addIncludeFilter(new AssignableTypeFilter(adn.model.entities.Entity.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ENTITY_PACKAGE)) {
				Class<? extends adn.model.entities.Entity> clazz = (Class<? extends adn.model.entities.Entity>) Class
						.forName(beanDef.getBeanClassName());
				Stack<Class<?>> stack = TypeHelper.getClassStack(clazz);

				while (!stack.isEmpty()) {
					this.entityTree.add((Class<adn.model.entities.Entity>) stack.pop());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
		this.entityTree.forEach(tree -> {
			logger.info(String.format("[%s] added to Entity tree %s", tree.getNode().getName(), (tree.getParent() == null ? " as super root"
							: String.format("with root [%s]", tree.getParent().getNode().getName()))));
		});
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	public void initializeModelTree() {
		this.modelTree = new ModelInheritanceTree<>(null, Model.class, null);

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.MODEL_PACKAGE)) {
				Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(beanDef.getBeanClassName());
				Stack<Class<?>> stack = TypeHelper.getClassStack(clazz);

				while (!stack.isEmpty()) {
					this.modelTree.add((Class<Model>) stack.pop());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
		this.modelTree.forEach(tree -> {
			logger.info(String.format("[%s] added to Model tree %s", tree.getNode().getName(), (tree.getParent() == null ? " as super root"
							: String.format(" with root [%s]", tree.getParent().getNode().getName()))));
		});
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private void initializeRelationMap() {
		this.relationMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		HashSet<Class<? extends Model>> models = new HashSet<>();
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));
		scanner.addIncludeFilter(new AnnotationTypeFilter(Generic.class));
		scanner.findCandidateComponents(Constants.MODEL_PACKAGE).stream().map(bean -> {
			try {
				Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(bean.getBeanClassName());

				if (!TypeHelper.isExtendedFrom(clazz, Model.class)) {
					throw new Exception(clazz.getName() + " is a Non-standard Model. A Model must be extended from "
							+ Model.class);
				}

				models.add(clazz);

				return clazz;
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());

				return null;
			}
		}).forEach(clazz -> {
			try {
				Field[] fields = clazz.getDeclaredFields();

				for (Field f : fields) {
					if (TypeHelper.isExtendedFrom(f.getType(), AbstractModel.class) && models.contains(clazz)
							&& this.entityTree.contains((Class<? extends Model>) f.getType())) {
						throw new Exception(clazz.getName() + " is a Non-standard Model. " + f.getType().getName()
								+ " was modelized into a Model. Use the modelized type instead");
					}

					if (TypeHelper.isImplementedFrom(f.getType(), Collection.class)) {
						ParameterizedType type = (ParameterizedType) f.getGenericType();
						Class<?> clz = (Class<?>) type.getActualTypeArguments()[0];

						if (TypeHelper.isExtendedFrom(clz, Model.class) && models.contains(clazz)
								&& this.entityTree.contains((Class<? extends Model>) clz)) {
							throw new Exception(clazz.getName() + " is a Non-standard Model. " + clz.getName()
									+ " was modelized into a Model. Use the modelized type instead on field: "
									+ f.getName());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		});
		models.forEach(clazz -> {
			Generic annotaion = clazz.getDeclaredAnnotation(Generic.class);
			Class<? extends adn.model.entities.Entity> relatedClass = annotaion.entityGene();

			if (this.relationMap.get(relatedClass) == null) {
				this.relationMap.put(relatedClass, Set.of(clazz));
			} else {
				Set<Class<? extends AbstractModel>> set = this.relationMap.get(relatedClass).stream()
						.collect(Collectors.toSet());

				set.add(clazz);
				this.relationMap.put(relatedClass, set);
			}
		});
		entityTree.forEach(branch -> {
			if (relationMap.get(branch.getNode()) == null) {
				relationMap.put(branch.getNode(), Set.of(branch.getNode()));
			}
		});
		// @formatter:on
		this.relationMap.forEach((key, val) -> val.forEach(clazz -> logger.info(
				String.format("[%s] related to [%s]", key.getName(), key.equals(clazz) ? "itself" : clazz.getName()))));
	}

	@SuppressWarnings("unchecked")
	private void initializeDefaultModelMap() {
		this.defaultModelMap = new HashMap<>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Model.class));
		scanner.findCandidateComponents(Constants.MODEL_PACKAGE)
			.forEach(bean -> {
				try {
					Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(bean.getBeanClassName());
					Order order = clazz.getDeclaredAnnotation(Order.class);
	
					if (order == null || order.value() != Ordered.HIGHEST_PRECEDENCE) {
						return;
					}
	
					this.defaultModelMap.put(clazz.getDeclaredAnnotation(Generic.class).entityGene(), clazz);
				} catch (Exception e) {
					e.printStackTrace();
					SpringApplication.exit(ContextProvider.getApplicationContext());
				}
			});
		entityTree.forEach(branch -> {
			if (defaultModelMap.get(branch.getNode()) == null) {
				defaultModelMap.put(branch.getNode(), branch.getNode());
			}
		});
		// @formatter:on
		this.defaultModelMap.forEach((k, v) -> logger
				.info(String.format("[%s] is default for [%s]", v.getName(), k.equals(v) ? "itself" : k.getName())));
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

	public Map<Class<? extends adn.model.entities.Entity>, Set<Class<? extends AbstractModel>>> getRelationMap() {
		return relationMap;
	}

	public Class<? extends AbstractModel> getModelClass(Class<? extends adn.model.entities.Entity> entityClass) {
		return this.defaultModelMap.get(entityClass);
	}

	@SuppressWarnings("unchecked")
	public <T extends adn.model.entities.Entity> T instantiate(Class<T> type) {
		return (T) ((AbstractEntityTuplizer) getTuplizer(type)).instantiate();
	}

	@SuppressWarnings("unchecked")
	public <T extends adn.model.entities.Entity> T instantiate(Class<T> type, Serializable id) {
		return (T) ((AbstractEntityTuplizer) getTuplizer(type)).instantiate(id);
	}

	private <T extends adn.model.entities.Entity> EntityTuplizer getTuplizer(Class<T> type) {
		return sessionFactory.unwrap(SessionFactoryImplementor.class).getMetamodel().locateEntityPersister(type)
				.getEntityTuplizer();
	}

}
