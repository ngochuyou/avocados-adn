/**
 * 
 */
package adn.application.context.builders;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.Generic;
import adn.model.ModelInheritanceTree;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.entities.metadata.DomainEntityMetadataImpl;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ModelContextProvider implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private ModelInheritanceTree<DomainEntity> entityTree;
	private ModelInheritanceTree<Model> modelTree;

	private Map<Class<? extends DomainEntity>, Set<Class<? extends DomainEntity>>> relationMap;
	private Map<Class<? extends DomainEntity>, Class<? extends DomainEntity>> defaultModelMap;

	private Map<Class<? extends DomainEntity>, DomainEntityMetadata<? extends DomainEntity>> metadataMap;

	@Override
	public void buildAfterStartUp() {
		logger.info("Building " + this.getClass());
		initializeEntityTree();
		logger.debug("---------------------------------------");
		initializeModelTree();
		logger.debug("---------------------------------------");
		initializeRelationMap();
		logger.debug("---------------------------------------");
		initializeDefaultModelMap();
		logger.debug("---------------------------------------");
		initializeMetadataMap();
		logger.info("Finished building " + this.getClass());
	}

	private void initializeMetadataMap() {
		metadataMap = new HashMap<>();
		entityTree.forEach(
				branch -> metadataMap.put(branch.getNode(), new DomainEntityMetadataImpl<>(this, branch.getNode())));
		entityTree.forEach(branch -> logger.debug(
				String.format("%s -> %s", branch.getNode().getName(), metadataMap.get(branch.getNode()).toString())));
	}

	@SuppressWarnings("unchecked")
	private void initializeEntityTree() {
		this.entityTree = new ModelInheritanceTree<>(null, DomainEntity.class, null);

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(DomainEntity.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ROOT_PACKAGE)) {
				Class<? extends DomainEntity> clazz = (Class<? extends DomainEntity>) Class
						.forName(beanDef.getBeanClassName());
				Stack<?> stack = TypeHelper.getClassStack(clazz);

				while (!stack.isEmpty()) {
					this.entityTree.add((Class<DomainEntity>) stack.pop());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
		this.entityTree.forEach(tree -> {
			logger.debug(String.format("[%s] added to Entity tree %s", tree.getNode().getName(), (tree.getParent() == null ? " as super root"
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
				Stack<?> stack = TypeHelper.getClassStack(clazz);

				while (!stack.isEmpty()) {
					this.modelTree.add((Class<Model>) stack.pop());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
		this.modelTree.forEach(tree -> {
			logger.debug(String.format("[%s] added to Model tree %s", tree.getNode().getName(), (tree.getParent() == null ? " as super root"
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
					if (TypeHelper.isExtendedFrom(f.getType(), DomainEntity.class) && models.contains(clazz)
							&& this.entityTree.contains((Class<? extends Model>) f.getType())) {
						throw new Exception(clazz.getName() + " is a Non-standard Model. " + f.getType().getName()
								+ " was modelized into a Model. Use the modelized type instead");
					}

					if (TypeHelper.isImplementedFrom(f.getType(), Collection.class)) {
						Class<?> clz = (Class<?>) TypeHelper.getGenericType(f);

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
			Generic annotation = clazz.getDeclaredAnnotation(Generic.class);

			Class<? extends adn.model.entities.Entity> relatedClass = (Class<? extends adn.model.entities.Entity>) annotation.entityGene();

			if (this.relationMap.get(relatedClass) == null) {
				this.relationMap.put(relatedClass, Set.of(clazz));
			} else {
				Set<Class<? extends DomainEntity>> set = this.relationMap.get(relatedClass).stream()
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
		this.relationMap.forEach((key, val) -> val.forEach(clazz -> logger.debug(
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
	
					this.defaultModelMap.put((Class<? extends adn.model.entities.Entity>) clazz.getDeclaredAnnotation(Generic.class).entityGene(), clazz);
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
				.debug(String.format("[%s] is default for [%s]", v.getName(), k.equals(v) ? "itself" : k.getName())));
	}

	public ModelInheritanceTree<DomainEntity> getEntityTree() {
		return entityTree;
	}

	public void setEntityTree(ModelInheritanceTree<DomainEntity> entityTree) {
		this.entityTree = entityTree;
	}

	public ModelInheritanceTree<Model> getModelTree() {
		return modelTree;
	}

	public Map<Class<? extends DomainEntity>, Set<Class<? extends DomainEntity>>> getRelationMap() {
		return relationMap;
	}

	public void setRelationMap(Map<Class<? extends DomainEntity>, Set<Class<? extends DomainEntity>>> relationMap) {
		this.relationMap = relationMap;
	}

	public Class<? extends DomainEntity> getModelClass(Class<? extends adn.model.entities.Entity> entityClass) {
		return this.defaultModelMap.get(entityClass);
	}

	@SuppressWarnings("unchecked")
	public <T extends DomainEntity> T instantiate(Class<T> type) {
		try {
			return type.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return (T) new DomainEntity() {};
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends DomainEntity> DomainEntityMetadata<T> getMetadata(Class<T> entityType) {
		return (DomainEntityMetadata<T>) metadataMap.get(entityType);
	}

	@Override
	public void afterBuild() {
		this.logger = null;
	}

}
