/**
 * 
 */
package adn.application.context.builders;

import static adn.application.context.ContextProvider.getBean;
import static java.time.LocalDateTime.now;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.Generic;
import adn.model.entities.ApprovableResource;
import adn.model.entities.AuditableResource;
import adn.model.entities.Entity;
import adn.model.entities.NamedResource;
import adn.model.entities.Operator;
import adn.service.entity.builder.EntityBuilderContract;
import adn.service.entity.builder.EntityBuilder;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class EntityBuilderProvider implements ContextBuilder {

	private static final String DEFAULT_BUILDER_NAME = "DEFAULT_BUILDER";

	private Map<Class<? extends Entity>, EntityBuilder<? extends Entity>> builderMap;
	private static final EntityBuilder<Entity> DEFAULT_BUILDER = new EntityBuilderContract<Entity>() {

		@Override
		public <E extends Entity> E buildUpdate(Serializable id, E entity, E persistence, Session session) {
			return persistence;
		}

		@Override
		public <E extends Entity> E buildInsertion(Serializable id, E entity, Session session) {
			return entity;
		}

		@Override
		public String getLoggableName() {
			return DEFAULT_BUILDER_NAME;
		}

		@Override
		public <E extends Entity> E buildPostValidationOnInsert(Serializable id, E model, Session session) {
			return model;
		}

	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void buildAfterStartUp() {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass().getName());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(EntityBuilder.class));
		Map<Class<? extends Entity>, EntityBuilder<? extends Entity>> builderMap = new HashMap<>();

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

			final ModelContextProvider modelContex = getBean(ModelContextProvider.class);
			final AuthenticationService authService = getBean(AuthenticationService.class);
			// @formatter:off
			final Map<Class, EntityBuilder> fixedBuilderMap = Map.of(
					NamedResource.class, new NamedResourceBuilder(),
					AuditableResource.class, new AuditableResourceBuilder(authService),
					ApprovableResource.class, new ApprovableResourceBuilder()
					);
			// @formatter:on
			modelContex.getEntityTree().forEach(branch -> {
				Class<? extends Entity> type = (Class<? extends Entity>) branch.getNode();

				_try: try {
					if (builderMap.get(branch.getNode()) == null) {
						if (branch.getParent() == null) {
							builderMap.put(type, DEFAULT_BUILDER);
							break _try;
						}

						if (!builderMap.containsKey(branch.getParent().getNode())) {
							builderMap.put(type, DEFAULT_BUILDER);
							break _try;
						}

						builderMap.put(type, builderMap.get(branch.getParent().getNode()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				EntityBuilder<? extends DomainEntity> builder = builderMap.get(type);
				EntityBuilder fixedBuilder;

				for (Class<?> interfaceType : ClassUtils.getAllInterfacesForClassAsSet(type)) {
					if (fixedBuilderMap.containsKey(interfaceType)) {
						fixedBuilder = fixedBuilderMap.get(interfaceType);

						if (!builder.equals(DEFAULT_BUILDER)) {
							builder = fixedBuilder.and(builder);
							continue;
						}

						builder = fixedBuilder;
					}
				}

				builderMap.put(type, builder);
			});

			modelContex.getEntityTree().forEach(node -> {
				Class<? extends DomainEntity> type = node.getNode();
				// @formatter:off
				logger.debug(String.format("[%s<%s>] -> %s",
						type.getName(),
						Stream.of(ClassUtils.getAllInterfacesForClass(type))
							.map(Class::getSimpleName).collect(Collectors.joining(", ")),
						builderMap.get(type).getLoggableName()));
				// @formatter:on
			});
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		this.builderMap = Collections.unmodifiableMap(builderMap);

		logger.info("Finished building " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> EntityBuilder<T> getBuilder(Class<T> clazz) {
		return (EntityBuilder<T>) this.builderMap.get(clazz);
	}

	@SuppressWarnings("rawtypes")
	private class NamedResourceBuilder extends EntityBuilderContract {

		private Entity build(Entity target, Entity model) {
			NamedResource targetedResource = (NamedResource) target;
			NamedResource modelResource = (NamedResource) model;

			targetedResource.setName(StringHelper.normalizeString(modelResource.getName()));

			return (Entity) targetedResource;
		}

		@Override
		public Entity buildInsertion(Serializable id, Entity entity, Session session) {
			return build(entity, entity);
		}

		@Override
		public Entity buildUpdate(Serializable id, Entity entity, Entity persistence, Session session) {
			return build(persistence, entity);
		}

		@Override
		public Entity buildPostValidationOnInsert(Serializable id, Entity model, Session session) {
			return model;
		}

	}

	@SuppressWarnings("rawtypes")
	private class AuditableResourceBuilder extends EntityBuilderContract {

		private final AuthenticationService authService;

		public AuditableResourceBuilder(AuthenticationService authService) {
			super();
			this.authService = authService;
		}

		@Override
		public Entity buildInsertion(Serializable id, Entity entity, Session session) {
			AuditableResource<?> resource = (AuditableResource<?>) entity;
			Operator operator = authService.getOperator();

			resource.setCreatedBy(operator);
			resource.setCreatedDate(now());
			resource.setLastModifiedBy(operator);
			resource.setLastModifiedDate(now());

			return (Entity) resource;
		}

		@Override
		public Entity buildUpdate(Serializable id, Entity entity, Entity persistence, Session session) {
			AuditableResource<?> resource = (AuditableResource<?>) persistence;

			resource.setLastModifiedBy(authService.getOperator());
			resource.setLastModifiedDate(now());

			return (Entity) resource;
		}

		@Override
		public Entity buildPostValidationOnInsert(Serializable id, Entity model, Session session) {
			return model;
		}

	}

	@SuppressWarnings("rawtypes")
	private class ApprovableResourceBuilder extends EntityBuilderContract {

		@Override
		public Entity buildInsertion(Serializable id, Entity entity, Session session) {
			ApprovableResource resource = (ApprovableResource) entity;

			resource.setApprovedBy(null);
			resource.setApprovedTimestamp(null);

			return (Entity) resource;
		}

		@Override
		public Entity buildPostValidationOnInsert(Serializable id, Entity model, Session session) {
			return model;
		}

		@Override
		public Entity buildUpdate(Serializable id, Entity model, Entity persistence, Session session) {
			return persistence;
		}

	}

}
