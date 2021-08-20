/**
 * 
 */
package adn.application.context.builders;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.CollectionHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesProducer;
import adn.model.factory.property.production.SecuredProperty;
import adn.model.factory.property.production.authentication.AuthenticationBasedModelPropertiesProducerImpl;
import adn.model.factory.property.production.authentication.AuthenticationBasedModelPropertiesProducersBuilder;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component(DefaultAuthenticationBasedModelPropertiesProducerFactory.NAME)
@Primary
public class DefaultAuthenticationBasedModelPropertiesProducerFactory
		implements AuthenticationBasedModelPropertiesFactory, ContextBuilder {

	public static final String NAME = "DefaultAuthenticationBasedModelPropertiesProducerFactory";

	private Map<Class<? extends DomainEntity>, AuthenticationBasedModelPropertiesProducer> producers;

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass());

		ModelContextProvider modelContext = ContextProvider.getBean(ModelContextProvider.class);
		final AuthenticationBasedModelPropertiesProducersBuilderImpl builder = new AuthenticationBasedModelPropertiesProducersBuilderImpl(
				modelContext);
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(
				new AssignableTypeFilter(AuthenticationBasedModelPropertiesProducersContributor.class));

		AuthenticationBasedModelPropertiesProducersContributor contributor;

		for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ROOT_PACKAGE)) {
			try {
				Class<? extends AuthenticationBasedModelPropertiesProducersContributor> contributorClass = (Class<? extends AuthenticationBasedModelPropertiesProducersContributor>) Class
						.forName(beanDef.getBeanClassName());

				logger.info(String.format("Found one %s of type [%s]",
						AuthenticationBasedModelPropertiesProducersContributor.class.getSimpleName(),
						beanDef.getBeanClassName()));

				contributor = contributorClass.getConstructor().newInstance();
				contributor.contribute(builder);
			} catch (NoSuchMethodException nsm) {
				SpringApplication.exit(ContextProvider.getApplicationContext());
				throw new IllegalArgumentException(
						String.format("A non-arg constructor is required on a %s instance, unable to find one in [%s]",
								AuthenticationBasedModelPropertiesProducersContributor.class.getSimpleName(),
								beanDef.getBeanClassName()));
			} catch (Exception any) {
				any.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		}

		producers = new HashMap<>(0, 1f);
		modelContext.getEntityTree().forEach(branch -> {
			producers.put(branch.getNode(),
					new AuthenticationBasedModelPropertiesProducerImpl<>((Class<DomainEntity>) branch.getNode(),
							builder.propertiesMap.values().stream()
									.filter(prop -> TypeHelper.isParentOf(prop.getEntityType(), branch.getNode()))
									.map(prop -> (SecuredProperty<DomainEntity>) prop).collect(Collectors.toSet())));
		});
		producers = Collections.unmodifiableMap(producers);

		logger.info("Finished building " + this.getClass());

		producers.values().stream().forEach(producer -> producer.afterFactoryBuild(producers));
	}

	private <T extends DomainEntity> AuthenticationBasedModelPropertiesProducer getProducer(Class<T> type) {
		return producers.get(type);
	}

	@Override
	public <T extends DomainEntity> Map<String, Object> produce(Class<T> type, Object[] properties, String[] columns,
			Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(properties, role, columns);
	}

	@Override
	public <T extends DomainEntity> List<Map<String, Object>> produce(Class<T> type, List<Object[]> properties,
			String[] columns, Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(properties, role, columns);
	}

	@Override
	public <T extends DomainEntity> Map<String, Object> singularProduce(Class<T> type, Object source, String column,
			Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.singularProduce(source, role, column);
	}

	@Override
	public <T extends DomainEntity> List<Map<String, Object>> singularProduce(Class<T> type, List<Object> sources,
			String column, Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.singularProduce(sources, role, column);
	}

	@Override
	public <T extends DomainEntity> Collection<String> validateAndTranslateColumnNames(Class<T> type, Role role,
			Collection<String> requestedColumnNames) throws NoSuchFieldException {
		return getProducer(type).validateAndTranslateColumnNames(role, requestedColumnNames);
	}

	public class AuthenticationBasedModelPropertiesProducersBuilderImpl
			implements AuthenticationBasedModelPropertiesProducersBuilder {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private final ModelContextProvider modelContext;
		private final Map<Key<? extends DomainEntity>, SecuredProperty<? extends DomainEntity>> propertiesMap = Collections
				.synchronizedMap(new HashMap<>());

		private AuthenticationBasedModelPropertiesProducersBuilderImpl(ModelContextProvider modelContext) {
			this.modelContext = modelContext;
		}

		@Override
		public <T extends DomainEntity, E extends T> WithType<E> type(Class<E> type) {
			return new WithTypes<>(this, type);
		}

		private Function<Object, Object> getMasker() {
			return AuthenticationBasedModelPropertiesProducer.MASKER;
		}

		private Function<Object, Object> getPublisher() {
			return AuthenticationBasedModelPropertiesProducer.PUBLISHER;
		}

		private Function<Object, Object> getDefaultFunction() {
			return getPublisher();
		}

		private abstract class AbstractOwned implements Owned {

			protected final AuthenticationBasedModelPropertiesProducersBuilder owner;

			public AbstractOwned(AuthenticationBasedModelPropertiesProducersBuilder owner) {
				super();
				this.owner = owner;
			}

			@Override
			public AuthenticationBasedModelPropertiesProducersBuilder and() {
				return owner;
			}

		}

		public class WithTypes<T extends DomainEntity> extends AbstractOwned implements WithType<T> {

			protected final Class<? extends T> type;

			public <E extends T> WithTypes(AuthenticationBasedModelPropertiesProducersBuilder owner, Class<E> type) {
				super(owner);
				this.type = type;
				logger.trace(String.format("With type %s", type.getName()));
			}

			@Override
			public WithRole<T> role(Role... roles) {
				return new WithRoles(owner, this, roles);
			}

			protected WithField<T> anyRoles(Role[] roles, String... fields) {
				return new WithRoles(owner, this, roles).field(fields);
			}

			protected WithField<T> anyRoles(String... fields) {
				return new WithRoles(owner, this, getUngivenRoles()).field(fields);
			}

			protected WithType<T> apply(Role[] roles, Function<Object, Object> function) {
				DomainEntityMetadata metadata = modelContext.getMetadata(type);

				metadata.getDeclaredPropertyNames().forEach(prop -> {
					Stream.of(roles).forEach(role -> {
						propertiesMap.put(new Key<>(type, role, prop),
								new SecuredPropertyImpl<>(type, role, prop, function));
					});
				});

				return this;
			}

			protected WithType<T> apply(Function<Object, Object> function) {
				return apply(Role.values(), function);
			}

			@Override
			public WithType<T> mask() {
				logger.trace("Mask all");
				return apply(getMasker());
			}

			@Override
			public WithType<T> publish() {
				logger.trace("Publish all");
				return apply(getPublisher());
			}

			private Role[] getUngivenRoles() {
				Set<Role> ungiven = new HashSet<>();
				Set<Key<? extends DomainEntity>> keysByType = propertiesMap.keySet().stream()
						.filter(key -> key.type.equals(type)).collect(Collectors.toSet());

				Stream.of(Role.values()).forEach(role -> {
					for (Key<? extends DomainEntity> key : keysByType) {
						if (key.role.equals(role)) {
							return;
						}
					}

					ungiven.add(role);
				});

				return ungiven.toArray(Role[]::new);
			}

			@Override
			public WithRole<T> anyRoles() {
				return new WithRoles(owner, this, getUngivenRoles());
			}

			public class WithRoles extends AbstractOwned implements WithRole<T> {

				private final Role[] roles;
				private final WithTypes<T> owningType;

				public WithRoles(AuthenticationBasedModelPropertiesProducersBuilder owner, WithTypes<T> owningType,
						Role... roles) {
					super(owner);
					this.roles = roles;
					this.owningType = owningType;
					logger.trace(String.format("With roles %s",
							Stream.of(roles).map(role -> role.toString()).collect(Collectors.joining(", "))));
				}

				@Override
				public WithField<T> field(String... fieldNames) {
					return new WithFields(owner, this, fieldNames);
				}

				@Override
				public WithType<T> type() {
					return owningType;
				}

				private String[] getUngivenFields(String[] existingFields) {
					Set<String> ungiven = new HashSet<>();
					Set<String> given = Set.of(existingFields);
					DomainEntityMetadata metadata = modelContext.getMetadata(type);

					metadata.getDeclaredPropertyNames().forEach(prop -> {
						if (given.contains(prop)) {
							return;
						}

						ungiven.add(prop);
					});

					return ungiven.toArray(String[]::new);
				}

				@Override
				public WithRole<T> anyRoles() {
					return owningType.anyRoles();
				}

				@Override
				public WithField<T> anyFields(String[] existingFields) {
					return new WithFields(owner, this, getUngivenFields(existingFields));
				}

				@Override
				public WithField<T> anyFields() {
					return new WithFields(owner, this, getUngivenFields(CollectionHelper.EMPTY_STRING_ARRAY));
				}

				private class WithFields extends AbstractOwned implements WithField<T> {

					private final String[] names;
					private final WithRoles owningRole;

					public WithFields(AuthenticationBasedModelPropertiesProducersBuilder owner, WithRoles owningRole,
							String... names) {
						super(owner);
						this.names = names;
						this.owningRole = owningRole;
						logger.trace(
								String.format("With fields %s", Stream.of(names).collect(Collectors.joining(", "))));
					}

					protected <F, R> WithField<T> apply(Function<F, R> function) {
						Stream.of(roles).forEach(role -> {
							Stream.of(names).forEach(name -> {
								propertiesMap.merge(new Key<>(type, role, name),
										new SecuredPropertyImpl<>(type, role, name, function), (oldProp, newProp) -> {
											SecuredPropertyImpl<? extends DomainEntity> oldProperty = (SecuredPropertyImpl<? extends DomainEntity>) oldProp;

											oldProperty.setFunction(function);

											return oldProp;
										});
							});
						});

						return this;
					}

					@Override
					public WithField<T> use(String alternativeName) {
						logger.trace(String.format("Use alt name [%s]", alternativeName));

						Stream.of(roles).forEach(role -> {
							Stream.of(names).forEach(name -> {
								propertiesMap.merge(new Key<>(type, role, name), new SecuredPropertyImpl<>(type, role,
										name, alternativeName, getDefaultFunction()), (oldProperty, newProperty) -> {
											@SuppressWarnings("unchecked")
											SecuredPropertyImpl<T> oldProp = (SecuredPropertyImpl<T>) oldProperty;

											oldProp.setAlternativeName(alternativeName);

											return oldProp;
										});
							});
						});

						return this;
					}

					@Override
					public WithField<T> mask() {
						logger.trace("Mask all");
						return apply(getMasker());
					}

					@Override
					public WithField<T> publish() {
						logger.trace("Publish all");
						return apply(getPublisher());
					}

					@Override
					public <F, R> WithField<T> use(Function<F, R> function) {
						logger.trace(String.format("Use function [%s]", function));
						return apply(function);
					}

					@Override
					public WithField<T> role(Role... role) {
						return owningType.anyRoles(roles, names);
					}

					@Override
					public WithField<T> anyRoles() {
						return owningType.anyRoles(names);
					}

					@Override
					public WithField<T> field(String... fieldNames) {
						return owningRole.field(fieldNames);
					}

					@Override
					public WithField<T> anyFields() {
						return owningRole.anyFields(names);
					}

					@Override
					public WithField<T> but(String... excludedFields) {
						Set<String> excludedFieldSet = Set.of(excludedFields);

						logger.trace(String.format("Excluding %s",
								Stream.of(excludedFields).collect(Collectors.joining(", "))));

						return owningRole.field(Stream.of(names).filter(name -> !excludedFieldSet.contains(name))
								.toArray(String[]::new));
					}

					@Override
					public WithRole<T> role() {
						return owningRole;
					}

					@Override
					public WithType<T> type() {
						return owningType;
					}

				}

			}

		}

	}

	public class SecuredPropertyImpl<T extends DomainEntity> implements SecuredProperty<T> {

		private final Class<T> type;
		private final Role role;
		private final String originalName;
		private String alternativeName;
		private Function<Object, Object> function;

		public SecuredPropertyImpl(Class<T> type, Role role, String originalName) {
			super();
			this.type = type;
			this.role = role;
			this.originalName = originalName;
		}

		public <F, R> SecuredPropertyImpl(Class<T> type, Role role, String originalName, Function<F, R> function) {
			this(type, role, originalName);
			setFunction(function);
		}

		public <F, R> SecuredPropertyImpl(Class<T> type, Role role, String originalName, String alternativeName,
				Function<F, R> function) {
			this(type, role, originalName);
			setFunction(function);
			setAlternativeName(alternativeName);
		}

		@Override
		public Class<T> getEntityType() {
			return type;
		}

		@Override
		public String getPropertyName() {
			return originalName;
		}

		@Override
		public String getPropertyAlternativeName() {
			return alternativeName;
		}

		@Override
		public adn.service.internal.Role getRole() {
			return role;
		}

		@Override
		public Function<Object, Object> getFunction() {
			return function;
		}

		public void setAlternativeName(String alternativeName) {
			this.alternativeName = alternativeName;
		}

		@SuppressWarnings("unchecked")
		public <F, R> void setFunction(Function<F, R> function) {
			this.function = (Function<Object, Object>) function;
		}

	}

	private class Key<T extends DomainEntity> {

		private final Class<T> type;
		private final Role role;
		private final String originalName;

		private final int hashCode;

		private Key(Class<T> type, Role role, String originalName) {
			Assert.notNull(type, "Null type");
			Assert.notNull(originalName, "Null name");
			this.type = type;
			this.role = role;
			this.originalName = originalName;

			int hash = 17;

			hash += 37 * type.hashCode();
			hash += (role != null ? 37 * role.hashCode() : 0);
			hash += 37 * originalName.hashCode();

			hashCode = hash;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key)) {
				return false;
			}

			Key<?> that = (Key<?>) obj;

			if (this.role == null && that.role == null) {
				return this.type.equals(that.type) && this.originalName.equals(originalName);
			}

			return this.type.equals(that.type)
					&& (that.role == null ? this.role.equals(that.role) : that.role.equals(this.role))
					&& this.originalName.equals(originalName);
		}

	}

	public interface AuthenticationBasedModelPropertiesProducersContributor {

		void contribute(final AuthenticationBasedModelPropertiesProducersBuilder builder);

	}

}
