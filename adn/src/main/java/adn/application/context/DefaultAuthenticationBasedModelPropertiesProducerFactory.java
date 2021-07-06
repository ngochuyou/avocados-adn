/**
 * 
 */
package adn.application.context;

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
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

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
@Component
@Order(7)
public class DefaultAuthenticationBasedModelPropertiesProducerFactory
		implements AuthenticationBasedModelPropertiesFactory, ContextBuilder {

	private Map<Class<? extends AbstractModel>, AuthenticationBasedModelPropertiesProducer> producers;

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.trace(String.format("%s %s", ContextBuilder.super.getLoggingPrefix(this),
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

				logger.trace(String.format("Found one %s of type [%s]",
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

		logger.trace(String.format("%s %s", ContextBuilder.super.getLoggingPrefix(this),
				String.format("Finished building %s", this.getClass().getSimpleName())));
	}

	private <T extends AbstractModel> AuthenticationBasedModelPropertiesProducer getProducer(Class<T> type) {
		return producers.get(type);
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, Map<String, Object> properties) {
		return produce(type, properties, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produce(Class<T> type, Map<String, Object> properties,
			Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(properties, role);
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type,
			List<Map<String, Object>> properties) {
		return produce(type, properties, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type,
			List<Map<String, Object>> properties, Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(properties, role);
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produceImmutable(Class<T> type,
			Map<String, Object> properties) {
		return produceImmutable(type, properties, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> Map<String, Object> produceImmutable(Class<T> type, Map<String, Object> properties,
			Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produceImmutable(properties, role);
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produceImmutable(Class<T> type,
			List<Map<String, Object>> properties) {
		return produceImmutable(type, properties, ContextProvider.getPrincipalRole());
	}

	@Override
	public <T extends AbstractModel> List<Map<String, Object>> produceImmutable(Class<T> type,
			List<Map<String, Object>> properties, Role role) {
		AuthenticationBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produceImmutable(properties, role);
	}

	public class AuthenticationBasedModelPropertiesProducersBuilderImpl
			implements AuthenticationBasedModelPropertiesProducersBuilder {

		private final ModelContextProvider modelContext;
		private final Map<Key<? extends AbstractModel>, SecuredProperty<? extends AbstractModel>> propertiesMap = Collections
				.synchronizedMap(new HashMap<>());

		private AuthenticationBasedModelPropertiesProducersBuilderImpl(ModelContextProvider modelContext) {
			this.modelContext = modelContext;
		}

		@Override
		public <T extends AbstractModel> WithType<T> type(Class<T> type) {
			return new WithTypes<>(this, type);
		}

		@Override
		public <T extends AbstractModel, E extends T> WithType<E> types(
				@SuppressWarnings("unchecked") Class<E>... types) {
			return new WithTypes<>(this, types);
		}

		@Override
		public AuthenticationBasedModelPropertiesProducersBuilder mask() {
			Role[] roles = Role.values();

			modelContext.getEntityTree().forEach(branch -> {
				Stream.of(roles).forEach(role -> {
					EntityMetadata metadata = modelContext.getMetadata(branch.getNode());

					metadata.getPropertyNames().stream().forEach(prop -> {
						propertiesMap.put(new Key<>(branch.getNode(), role, prop),
								new SecuredPropertyImpl<>(branch.getNode(), role, prop, getMasker()));
					});
				});
			});

			return this;
		}

		@Override
		public AuthenticationBasedModelPropertiesProducersBuilder publish() {
			Role[] roles = Role.values();

			modelContext.getEntityTree().forEach(branch -> {
				Stream.of(roles).forEach(role -> {
					EntityMetadata metadata = modelContext.getMetadata(branch.getNode());

					metadata.getPropertyNames().stream().forEach(prop -> {
						propertiesMap.put(new Key<>(branch.getNode(), role, prop),
								new SecuredPropertyImpl<>(branch.getNode(), role, prop, getPublisher()));
					});
				});
			});

			return this;
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

		@Override
		public AuthenticationBasedModelPropertiesProducersBuilder maskUngivenTypes() {
			Role[] roles = Role.values();

			getUngivenTypes().forEach(type -> {
				Stream.of(roles).forEach(role -> {
					EntityMetadata metadata = modelContext.getMetadata(type);

					metadata.getPropertyNames().stream().forEach(prop -> {
						propertiesMap.put(new Key<>(type, role, prop),
								new SecuredPropertyImpl<>(type, role, prop, getMasker()));
					});
				});
			});

			return this;
		}

		@Override
		public AuthenticationBasedModelPropertiesProducersBuilder publishUngivenTypes() {
			Role[] roles = Role.values();

			getUngivenTypes().forEach(type -> {
				Stream.of(roles).forEach(role -> {
					EntityMetadata metadata = modelContext.getMetadata(type);

					metadata.getPropertyNames().stream().forEach(prop -> {
						propertiesMap.put(new Key<>(type, role, prop),
								new SecuredPropertyImpl<>(type, role, prop, getPublisher()));
					});
				});
			});

			return this;
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
			}

			@Override
			public WithRole<T> role(Role role) {
				return new WithRoles(owner, this, role);
			}

			@Override
			public WithRole<T> roles(Role... roles) {
				return new WithRoles(owner, this, roles);
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
				return apply(getMasker());
			}

			@Override
			public WithType<T> publish() {
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
			public WithType<T> maskUngivenRoles() {
				return apply(getUngivenRoles().toArray(Role[]::new), getMasker());
			}

			@Override
			public WithType<T> publishUngivenRoles() {
				return apply(getUngivenRoles().toArray(Role[]::new), getPublisher());
			}

			public class WithRoles extends AbstractOwned implements WithRole<T> {

				private final Role[] roles;
				private final WithType<T> owningType;

				public WithRoles(AuthenticationBasedModelPropertiesProducersBuilder owner, WithType<T> owningType,
						Role... roles) {
					super(owner);
					this.roles = roles;
					this.owningType = owningType;
				}

				@Override
				public WithField<T> field(String fieldName) {
					return new WithFields(owner, this, fieldName);
				}

				@Override
				public WithField<T> fields(String... fieldNames) {
					return new WithFields(owner, this, fieldNames);
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
					return apply(getMasker());
				}

				@Override
				public WithRole<T> publish() {
					return apply(getPublisher());
				}

				@Override
				public WithType<T> more() {
					return owningType;
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
				public WithRole<T> maskUngivenFields() {
					return apply(getUngivenFields().toArray(String[]::new), getMasker());
				}

				@Override
				public WithRole<T> publishUngivenFields() {
					return apply(getUngivenFields().toArray(String[]::new), getPublisher());
				}

				private class WithFields extends AbstractOwned implements WithField<T> {

					private final String[] names;
					private final WithRole<T> owningRole;

					public WithFields(AuthenticationBasedModelPropertiesProducersBuilder owner, WithRole<T> owningRole,
							String... names) {
						super(owner);
						this.names = names;
						this.owningRole = owningRole;
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
						return apply(getMasker());
					}

					@Override
					public WithField<T> publish() {
						return apply(getPublisher());
					}

					@Override
					public <F, R> WithField<T> use(Function<F, R> function) {
						return apply(function);
					}

					@Override
					public WithRole<T> more() {
						return owningRole;
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
