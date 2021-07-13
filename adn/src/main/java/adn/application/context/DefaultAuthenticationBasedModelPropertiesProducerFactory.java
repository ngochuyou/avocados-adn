/**
 * 
 */
package adn.application.context;

import java.sql.SQLSyntaxErrorException;
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
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.application.Constants;
import adn.helpers.TypeHelper;
import adn.model.AbstractModel;
import adn.model.ModelContextProvider;
import adn.model.entities.metadata.EntityMetadata;
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
@Order(7)
public class DefaultAuthenticationBasedModelPropertiesProducerFactory
		implements AuthenticationBasedModelPropertiesFactory, ContextBuilder {

	public static final String NAME = "DefaultAuthenticationBasedModelPropertiesProducerFactory";

	private Map<Class<? extends AbstractModel>, AuthenticationBasedModelPropertiesProducer> producers;

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info(String.format("%s %s", ContextBuilder.super.getLoggingPrefix(this),
				String.format("Building %s", this.getClass().getSimpleName())));

		ModelContextProvider modelContext;
		final AuthenticationBasedModelPropertiesProducersBuilderImpl builder = new AuthenticationBasedModelPropertiesProducersBuilderImpl(
				modelContext = ContextProvider.getBean(ModelContextProvider.class));
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
					new AuthenticationBasedModelPropertiesProducerImpl((Class<AbstractModel>) branch.getNode(),
							builder.propertiesMap.values().stream()
									.filter(prop -> TypeHelper.isParentOf(prop.getEntityType(), branch.getNode()))
									.map(prop -> (SecuredProperty<AbstractModel>) prop).collect(Collectors.toSet())));
		});

		logger.info(String.format("%s %s", ContextBuilder.super.getLoggingPrefix(this),
				String.format("Finished building %s", this.getClass().getSimpleName())));
	}

	private <T extends AbstractModel> AuthenticationBasedModelPropertiesProducer getProducer(Class<T> type) {
		return producers.get(type);
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, Object[] properties, String[] columns) {
		return produce(type, properties, columns, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, Object[] properties, String[] columns,
			Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(properties, role, columns);
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<Object[]> properties,
			String[] columns) {
		return produce(type, properties, columns, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<Object[]> properties,
			String[] columns, Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(properties, role, columns);
	}

	@Override
	public <T extends AbstractModel> String[] validateAndTranslateColumnNames(Class<T> type, Role role,
			String[] requestedColumnNames) throws SQLSyntaxErrorException {
		return getProducer(type).validateAndTranslateColumnNames(role, requestedColumnNames);
	}

	public class AuthenticationBasedModelPropertiesProducersBuilderImpl
			implements AuthenticationBasedModelPropertiesProducersBuilder {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private final ModelContextProvider modelContext;
		private final Map<Key<? extends AbstractModel>, SecuredProperty<? extends AbstractModel>> propertiesMap = Collections
				.synchronizedMap(new HashMap<>());

		private AuthenticationBasedModelPropertiesProducersBuilderImpl(ModelContextProvider modelContext) {
			this.modelContext = modelContext;
		}

		@Override
		public <T extends AbstractModel, E extends T> WithType<E> type(Class<E> type) {
			return new WithTypes<>(this, type);
		}

		@Override
		public <T extends AbstractModel, E extends T> WithType<E> type(Class<E>[] types) {
			return new WithTypes<>(this, types);
		}

		private AuthenticationBasedModelPropertiesProducersBuilder apply(Function<Object, Object> func) {
			Role[] roles = Role.values();

			modelContext.getEntityTree().forEach(branch -> {
				Stream.of(roles).forEach(role -> {
					EntityMetadata metadata = modelContext.getMetadata(branch.getNode());

					metadata.getPropertyNames().stream().forEach(prop -> {
						propertiesMap.put(new Key<>(branch.getNode(), role, prop),
								new SecuredPropertyImpl<>(branch.getNode(), role, prop, func));
					});
				});
			});

			return this;
		}

		@Override
		public AuthenticationBasedModelPropertiesProducersBuilder mask() {
			return apply(getMasker());
		}

		@Override
		public AuthenticationBasedModelPropertiesProducersBuilder publish() {
			return apply(getPublisher());
		}

		private Set<Class<? extends AbstractModel>> getUngivenTypes() {
			Set<Class<? extends AbstractModel>> ungiven = new HashSet<>();

			modelContext.getEntityTree().forEach(branch -> {
				for (Key<? extends AbstractModel> key : propertiesMap.keySet()) {
					if (branch.getNode().equals(key.type)) {
						return;
					}
				}

				ungiven.add(branch.getNode());
			});

			return ungiven;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends AbstractModel> WithType<T> ungivenTypes() {
			return new WithTypes<T>(this, (Class<T>[]) getUngivenTypes().toArray(Class<?>[]::new));
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

		public class WithTypes<T extends AbstractModel> extends AbstractOwned implements WithType<T> {

			protected final Class<? extends T>[] types;

			@SafeVarargs
			public <E extends T> WithTypes(AuthenticationBasedModelPropertiesProducersBuilder owner,
					Class<E>... types) {
				super(owner);
				this.types = types;
				logger.trace(String.format("With types %s",
						Stream.of(types).map(type -> type.getSimpleName()).collect(Collectors.joining(", "))));
			}

			@Override
			public WithRole<T> role(Role... roles) {
				return new WithRoles(owner, this, roles);
			}

			protected WithField<T> anyRoles(Role[] roles, String... fields) {
				return new WithRoles(owner, this, roles).field(fields);
			}

			protected WithField<T> anyRoles(String... fields) {
				return new WithRoles(owner, this, getUngivenRoles().toArray(Role[]::new)).field(fields);
			}

			protected WithType<T> apply(Role[] roles, Function<Object, Object> function) {
				Stream.of(types).forEach(type -> {
					EntityMetadata metadata = modelContext.getMetadata(type);

					metadata.getPropertyNames().forEach(prop -> {
						Stream.of(roles).forEach(role -> {
							propertiesMap.put(new Key<>(type, role, prop),
									new SecuredPropertyImpl<>(type, role, prop, function));
						});
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

			protected Set<Role> getUngivenRoles() {
				Set<Role> ungiven = new HashSet<>();
				Set<Key<? extends AbstractModel>> keysByType = propertiesMap.keySet().stream()
						.filter(key -> Stream.of(types).filter(type -> type.equals(key.type)).count() != 0)
						.collect(Collectors.toSet());

				Stream.of(Role.values()).forEach(role -> {
					for (Key<? extends AbstractModel> key : keysByType) {
						if (key.role.equals(role)) {
							return;
						}
					}

					ungiven.add(role);
				});

				return ungiven;
			}

			@Override
			public WithRole<T> anyRoles() {
				return new WithRoles(owner, this, getUngivenRoles().toArray(Role[]::new));
			}

			protected WithField<T> ungivenRoles(String... fields) {
				return new WithRoles(owner, this, getUngivenRoles().toArray(Role[]::new)).field(fields);
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

				protected WithRole<T> apply(String[] fields, Function<Object, Object> function) {
					Stream.of(types).forEach(type -> {
						Stream.of(roles).forEach(role -> {
							Stream.of(fields).forEach(prop -> {
								propertiesMap.put(new Key<>(type, role, prop),
										new SecuredPropertyImpl<>(type, role, prop, function));
							});
						});
					});

					return this;
				}

				protected WithRole<T> apply(Function<Object, Object> function) {
					Stream.of(types).forEach(type -> {
						apply(modelContext.getMetadata(type).getPropertyNames().toArray(String[]::new), function);
					});

					return this;
				}

				@Override
				public WithRole<T> mask() {
					logger.trace("Mask all");
					return apply(getMasker());
				}

				@Override
				public WithRole<T> publish() {
					logger.trace("Publish all");
					return apply(getPublisher());
				}

				protected Set<String> getUngivenFields() {
					final Set<String> ungiven = new HashSet<>();
					Set<Key<? extends AbstractModel>> keys = propertiesMap.keySet().stream()
							.filter(key -> Stream.of(types).filter(type -> type.equals(key.type)).count() != 0
									&& Stream.of(roles)
											.filter(role -> (key.role == null && role == null ? true
													: key.role != null ? key.role.equals(role) : role.equals(key.role)))
											.count() != 0)
							.collect(Collectors.toSet());

					Stream.of(types).forEach(type -> {
						EntityMetadata metadata = modelContext.getMetadata(type);

						metadata.getPropertyNames().forEach(prop -> {
							for (Key<? extends AbstractModel> key : keys) {
								if (prop.equals(key.originalName)) {
									return;
								}
							}

							ungiven.add(prop);
						});
					});

					return ungiven;
				}

				@Override
				public WithRole<T> anyRoles() {
					return owningType.anyRoles();
				}

				@Override
				public WithField<T> anyFields() {
					return new WithFields(owner, this, getUngivenFields().toArray(String[]::new));
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
						Stream.of(types).forEach(type -> {
							Stream.of(roles).forEach(role -> {
								Stream.of(names).forEach(name -> {
									propertiesMap.merge(new Key<>(type, role, name),
											new SecuredPropertyImpl<>(type, role, name, function),
											(oldProp, newProp) -> {
												SecuredPropertyImpl<? extends AbstractModel> oldProperty = (SecuredPropertyImpl<? extends AbstractModel>) oldProp;

												oldProperty.setFunction(function);

												return oldProp;
											});
								});
							});
						});

						return this;
					}

					@Override
					public WithField<T> use(String alternativeName) {
						logger.trace(String.format("Use alt name [%s]", alternativeName));
						Stream.of(types).forEach(type -> {
							Stream.of(roles).forEach(role -> {
								Stream.of(names).forEach(name -> {
									propertiesMap.merge(
											new Key<>(type, role, name), new SecuredPropertyImpl<>(type, role, name,
													alternativeName, getDefaultFunction()),
											(oldProperty, newProperty) -> {
												@SuppressWarnings("unchecked")
												SecuredPropertyImpl<T> oldProp = (SecuredPropertyImpl<T>) oldProperty;

												oldProp.setAlternativeName(alternativeName);

												return oldProp;
											});
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
					public WithField<T> role(Role role) {
						return owningType.anyRoles(new Role[] { role }, names);
					}

					@Override
					public WithField<T> roles(Role... roles) {
						return owningType.anyRoles(roles, names);
					}

					@Override
					public WithField<T> anyRoles() {
						return owningType.anyRoles(names);
					}

					@Override
					public WithField<T> field(String fieldName) {
						return owningRole.field(fieldName);
					}

					@Override
					public WithField<T> fields(String... fieldNames) {
						return owningRole.field(fieldNames);
					}

					@Override
					public WithField<T> anyFields() {
						return owningRole.anyFields();
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

	public class SecuredPropertyImpl<T extends AbstractModel> implements SecuredProperty<T> {

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

	private class Key<T extends AbstractModel> {

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
