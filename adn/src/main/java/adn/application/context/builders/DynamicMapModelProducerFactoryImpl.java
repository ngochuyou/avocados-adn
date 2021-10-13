/**
 * 
 */
package adn.application.context.builders;

import static adn.model.factory.authentication.ModelProducer.MASKER;
import static adn.model.factory.authentication.ModelProducer.PUBLISHER;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.LoggerHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.ModelProducerFactoryBuilder;
import adn.model.factory.authentication.SecuredProperty;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.BatchedPojoSourceImpl;
import adn.model.factory.authentication.dynamicmap.BatchedSourceImpl;
import adn.model.factory.authentication.dynamicmap.DynamicMapModelProducer;
import adn.model.factory.authentication.dynamicmap.DynamicMapModelProducerImpl;
import adn.model.factory.authentication.dynamicmap.SinglePojoSourceImpl;
import adn.model.factory.authentication.dynamicmap.SingleSourceImpl;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class DynamicMapModelProducerFactoryImpl implements DynamicMapModelProducerFactory, ContextBuilder {

	private Map<Class<? extends DomainEntity>, DynamicMapModelProducer<? extends DomainEntity>> producersMap;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info(String.format("Building %s", this.getClass()));

		ModelContextProvider modelContext = ContextProvider.getBean(ModelContextProvider.class);
		ModelProducerFactoryBuilderImpl builder = new ModelProducerFactoryBuilderImpl(modelContext);
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(ModelProducerFactoryContributor.class));

		ModelProducerFactoryContributor contributor;

		for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ROOT_PACKAGE)) {
			try {
				Class<? extends ModelProducerFactoryContributor> contributorClass = (Class<? extends ModelProducerFactoryContributor>) Class
						.forName(beanDef.getBeanClassName());

				logger.debug(String.format("Found one %s of type [%s] in [%s]",
						ModelProducerFactoryContributor.class.getSimpleName(), contributorClass.getName(),
						contributorClass.getPackageName()));

				contributor = contributorClass.getConstructor().newInstance();
				contributor.contribute(builder);
			} catch (NoSuchMethodException nsm) {
				SpringApplication.exit(ContextProvider.getApplicationContext());
				throw new IllegalArgumentException(String.format(
						"A non-arg constructor is required on a(n) %s instance, unable to find one in [%s]",
						ModelProducerFactoryContributor.class.getSimpleName(), beanDef.getBeanClassName()));
			} catch (Exception any) {
				any.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		}

		Map<Class<? extends DomainEntity>, DynamicMapModelProducer<? extends DomainEntity>> producersMap = new HashMap<>(
				16, 1.175f);

		modelContext.getEntityTree().forEach(branch -> {
			Class<DomainEntity> type = (Class<DomainEntity>) branch.getNode();

			producersMap.put(branch.getNode(),
					new DynamicMapModelProducerImpl<>(type,
							builder.properties.values().stream()
									.filter(prop -> TypeHelper.isParentOf(prop.getOwningType(), branch.getNode()))
									.map(prop -> (SecuredProperty<DomainEntity>) prop).collect(Collectors.toSet()),
							modelContext.getMetadata(type), this));
		});

		this.producersMap = Collections.unmodifiableMap(producersMap);

		logger.info(String.format("Finished building %s", this.getClass()));
	}

	@Override
	public <T extends DomainEntity, E extends T> Map<String, Object> produce(Object[] source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential {
		DynamicMapModelProducer<E> producer = getProducer(metadata.getEntityType());

		return producer.produceSingleSource(new SingleSourceImpl<>(metadata, source), credential);
	}

	@Override
	public <T extends DomainEntity, E extends T> List<Map<String, Object>> produce(List<Object[]> source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential {
		DynamicMapModelProducer<E> producer = getProducer(metadata.getEntityType());

		return producer.produceBatchedSource(new BatchedSourceImpl<>(metadata, source), credential);
	}

	@Override
	public <T extends DomainEntity, E extends T> Map<String, Object> produceSingular(Object source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential {
		return produce(new Object[] { source }, metadata, credential);
	}

	@Override
	public <T extends DomainEntity, E extends T> List<Map<String, Object>> produceSingular(List<Object> source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential {
		return produce(source.stream().map(col -> new Object[] { col }).collect(Collectors.toList()), metadata,
				credential);
	}

	@Override
	public <T extends DomainEntity, E extends T> Map<String, Object> producePojo(E entity, SourceMetadata<E> metadata,
			Credential credential) throws UnauthorizedCredential {
		DynamicMapModelProducer<E> producer = getProducer(metadata.getEntityType());

		return producer.produceSinglePojo(new SinglePojoSourceImpl<E>(metadata, entity), credential);
	}

	@Override
	public <T extends DomainEntity, E extends T> List<Map<String, Object>> producePojo(List<E> source,
			SourceMetadata<E> metadata, Credential credential) throws UnauthorizedCredential {
		DynamicMapModelProducer<E> producer = getProducer(metadata.getEntityType());

		return producer.produceBatchedPojo(new BatchedPojoSourceImpl<>(metadata, source), credential);
	}

	@Override
	public <T extends DomainEntity> List<String> validateColumns(Class<T> entityType,
			Collection<String> requestedColumnNames, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return getProducer(entityType).validateColumns(credential, requestedColumnNames);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DomainEntity> DynamicMapModelProducer<T> getProducer(Class<T> entityType) {
		return (DynamicMapModelProducer<T>) producersMap.get(entityType);
	}

	public interface ModelProducerFactoryContributor {

		void contribute(ModelProducerFactoryBuilder builder);

	}

	private class ModelProducerFactoryBuilderImpl implements ModelProducerFactoryBuilder {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private final ModelContextProvider modelContext;
		private final Map<Key<? extends DomainEntity>, SecuredPropertyImpl<? extends DomainEntity>> properties = new HashMap<>();

		public ModelProducerFactoryBuilderImpl(ModelContextProvider modelContext) {
			super();
			this.modelContext = modelContext;
		}

		@Override
		public <T extends DomainEntity> WithType<T> type(Class<T> type) {
			return new WithTypeImpl<>(type);
		}

		private <T extends DomainEntity> Key<T> resolveKey(Class<T> type, Role role, UUID departmentId,
				Credential credential, String name) {
			if (credential != null) {
				return new Key<>(type, credential, name);
			}

			return new Key<>(type, CredentialFactory.compound(role, departmentId), name);
		}

		private void modifyAlias(SecuredPropertyImpl<?> property, String newAlias) {
			logger.debug(String.format("Overring alias [%s] with [%s]", property.getAlias(), newAlias));
			property.setAlias(newAlias);
		}

		@SuppressWarnings("unchecked")
		private <T extends DomainEntity> SecuredPropertyImpl<T> locateProperty(Key<T> key) {
			if (properties.containsKey(key)) {
				logger.trace(String.format("Existing entry with %s", key.toString()));

				return (SecuredPropertyImpl<T>) properties.get(key);
			}

			return null;
		}

		private <T extends DomainEntity> SecuredPropertyImpl<T> putProperty(Key<T> key,
				SecuredPropertyImpl<T> property) {
			logger.trace(String.format("New entry with %s", key.toString()));
			properties.put(key, property);

			return property;
		}

		private <T extends DomainEntity> void setProperty(Class<T> type, Role role, UUID departmentId,
				Credential credential, String name, String alias) {
			Key<T> key = resolveKey(type, role, departmentId, credential, name);
			SecuredPropertyImpl<T> property = locateProperty(key);

			if (property != null) {
				modifyAlias(property, alias);
				return;
			}

			putProperty(key, new SecuredPropertyImpl<>(type, name, key.credential).setAlias(alias));
		}

		private void modifyFunction(SecuredPropertyImpl<?> property,
				HandledBiFunction<Arguments<?>, Credential, ?, Exception> newFnc) {
			logger.trace(String.format("Overring function [%s] with [%s]", property.getFunction(), newFnc));
			property.setFunction(newFnc);
		}

		private <T extends DomainEntity> void setProperty(Class<T> type, Role role, UUID departmentId,
				Credential credential, String name, HandledBiFunction<Arguments<?>, Credential, ?, Exception> fnc) {
			Key<T> key = resolveKey(type, role, departmentId, credential, name);
			SecuredPropertyImpl<T> property = locateProperty(key);

			if (property != null) {
				modifyFunction(property, fnc);
				return;
			}

			putProperty(key, new SecuredPropertyImpl<>(type, name, key.credential).setFunction(fnc));
		}

		private class WithTypeImpl<T extends DomainEntity> implements WithType<T> {

			private final Class<T> type;

			public WithTypeImpl(Class<T> type) {
				super();
				this.type = type;
				logger.debug(String.format("With type %s", type.getSimpleName()));
			}

			@Override
			public WithCredential<T> roles(Role... role) {
				return new WithCredentialImpl(this, role);
			}

			@Override
			public WithCredential<T> departments(UUID... departmentId) {
				return new WithCredentialImpl(this, departmentId);
			}

			@Override
			public WithCredential<T> with(Role[] role, UUID[] departmentId) {
				return new WithCredentialImpl(this, role, departmentId);
			}

			@Override
			public WithCredential<T> credentials(Credential... credential) {
				return new WithCredentialImpl(this, credential);
			}

			private class WithCredentialImpl implements WithCredential<T> {

				private final Set<String> remainingFields = new HashSet<>(
						modelContext.getMetadata(type).getPropertyNames());
				private final WithType<T> owningType;

				private Role[] roles;
				private UUID[] departmentIds;
				private Credential[] credentials;

				public WithCredentialImpl(WithType<T> owningType, Role... roles) {
					super();
					this.roles = roles;
					this.owningType = owningType;
					log(roles);
				}

				public WithCredentialImpl(WithType<T> owningType, UUID... departmentIds) {
					super();
					this.departmentIds = departmentIds;
					this.owningType = owningType;
					log(departmentIds);
				}

				public WithCredentialImpl(WithType<T> owningType, Role[] roles, UUID[] departmentIds) {
					super();
					this.roles = roles;
					this.departmentIds = departmentIds;
					this.owningType = owningType;
					log(roles, departmentIds);
				}

				public WithCredentialImpl(WithType<T> owningType, Credential... credentials) {
					super();
					this.credentials = credentials;
					this.owningType = owningType;
					log(credentials);
				}

				@Override
				public <E extends DomainEntity> WithType<E> type(Class<E> type) {
					return new WithTypeImpl<>(type);
				}

				private void setRoles(Role... roles) {
					this.roles = roles;
					log(roles);
				}

				@Override
				public WithCredential<T> roles(Role... roles) {
					return owningType.roles(roles);
				}

				private void setDepartmentIds(UUID... departmentIds) {
					this.departmentIds = departmentIds;
					log(departmentIds);
				}

				@Override
				public WithCredential<T> departments(UUID... departmentIds) {
					if (roles == null) {
						setDepartmentIds(departmentIds);
						return this;
					}

					UUID[] ids = requireNonNull(departmentIds);

					if (roles.length == 1) {
						setRoles(IntStream.range(0, ids.length).mapToObj(index -> roles[0]).toArray(Role[]::new));
						setDepartmentIds(ids);
						return this;
					}

					if (ids.length != roles.length) {
						throw new IllegalArgumentException(
								String.format("Roles length and Department IDs length must match. Roles[%d]><IDs[%d]",
										roles.length, ids.length));
					}

					setDepartmentIds(ids);
					return this;
				}

				@Override
				public WithCredential<T> with(Role[] roles, UUID[] departmentIds) {
					if (requireNonNull(roles).length != requireNonNull(departmentIds).length) {
						throw new IllegalArgumentException(
								String.format("Roles length and Department IDs length must match. Roles[%d]><IDs[%d]",
										roles.length, departmentIds.length));
					}

					this.roles = roles;
					this.departmentIds = departmentIds;
					log(roles, departmentIds);

					return this;
				}

				@Override
				public WithCredential<T> credentials(Credential... credentials) {
					this.roles = null;
					this.departmentIds = null;
					this.credentials = credentials;
					log(credentials);

					return this;
				}

//				private void log(Role roles) {
//					logger.debug(String.format("With role[%s]",
//							Stream.of(roles).map(role -> role.toString()).collect(Collectors.joining(","))));
//				}

				private void log(UUID... departmentIds) {
					logger.debug(String.format("With Department IDs[%s]", Stream.of(departmentIds)
							.map(departmentId -> departmentId.toString()).collect(Collectors.joining(","))));
				}

				private void log(Role[] roles, UUID[] departmentIds) {
					logger.debug(String.format("With roles[%s] and Department IDs[%s]",
							Stream.of(roles).map(role -> role.toString()).collect(Collectors.joining(",")),
							Stream.of(departmentIds).map(departmentId -> departmentId.toString())
									.collect(Collectors.joining(","))));
				}

				private void log(Credential... credentials) {
					logger.debug(String.format("With Credentials[%s]", Stream.of(credentials)
							.map(credential -> credential.evaluate()).collect(Collectors.joining(","))));
				}

				private Role getRole(int index) {
					return roles == null ? null : roles[index];
				}

				private UUID getDepartmentId(int index) {
					return departmentIds == null ? null : departmentIds[index];
				}

				private Credential getCredential(int index) {
					return credentials == null ? null : credentials[index];
				}

				private void removeRemainingFields(String... fields) {
					if (logger.isTraceEnabled()) {
						remainingFields
								.removeAll(LoggerHelper.with(logger)
										.trace(String.format("Removing [%s] from remaining fields",
												Stream.of(fields).collect(Collectors.joining(","))),
												Arrays.asList(fields)));
						return;
					}

					remainingFields.removeAll(Arrays.asList(fields));
				}

				@Override
				public WithField<T> fields(String... fields) {
					removeRemainingFields(fields);

					return new WithFieldImpl(this, fields);
				}

				private int getCredentialsAmount() {
					if (credentials != null) {
						return credentials.length;
					}

					return roles.length;
				}

				@Override
				public WithCredential<T> mask() {
					int n = getCredentialsAmount();

					IntStream.range(0, n)
							.forEach(index -> modelContext.getMetadata(type).getPropertyNames()
									.forEach(propName -> setProperty(type, getRole(index), getDepartmentId(index),
											getCredential(index), propName, MASKER)));

					logger.debug("Mask all");
					return this;
				}

				@Override
				public WithCredential<T> publish() {
					int n = getCredentialsAmount();

					IntStream.range(0, n)
							.forEach(index -> modelContext.getMetadata(type).getPropertyNames()
									.forEach(propName -> setProperty(type, getRole(index), getDepartmentId(index),
											getCredential(index), propName, PUBLISHER)));

					logger.debug("Publish all");
					return this;
				}

				private class WithFieldImpl implements WithField<T> {

					private final WithCredential<T> owningCredentials;

					private final String[] fields;

					public WithFieldImpl(WithCredential<T> owningCredentials, String[] fields) {
						super();
						this.fields = Stream.of(fields).map(Objects::requireNonNull).toArray(String[]::new);
						this.owningCredentials = owningCredentials;

						if (fields.length != 0 && logger.isDebugEnabled()) {
							logger.debug(String.format("With fields %s",
									Stream.of(fields).collect(Collectors.joining(", "))));
							return;
						}

						logger.trace("Requested fields were empty");
					}

					@Override
					public WithField<T> fields(String... fields) {
						return owningCredentials.fields(fields);
					}

					@Override
					public WithField<T> use(String... alias) {
						if (requireNonNull(alias).length != fields.length) {
							throw new IllegalArgumentException(String.format(
									"Alias names length and field lengths must match. Alias[%d]><[%d]Fields",
									alias.length, fields.length));
						}

						int n = getCredentialsAmount();

						IntStream.range(0, n)
								.forEach(index -> IntStream.range(0, fields.length)
										.forEach(i -> setProperty(type, getRole(index), getDepartmentId(index),
												getCredential(index), fields[i], alias[i])));

						logger.debug(
								String.format("Using alias %s", Stream.of(alias).collect(Collectors.joining(","))));

						return this;
					}

					@Override
					@SuppressWarnings("unchecked")
					public WithField<T> useFunction(HandledBiFunction<Arguments<?>, Credential, ?, Exception>... fncs) {
						if (fncs.length != 1 && requireNonNull(fncs).length != fields.length) {
							throw new IllegalArgumentException(String.format(
									"Functions length and field lengths must match. Functions[%d]><[%d]Fields",
									fncs.length, fields.length));
						}

						int n = getCredentialsAmount();

						IntStream.range(0, n)
								.forEach(index -> IntStream.range(0, fields.length)
										.forEach(i -> setProperty(type, getRole(index), getDepartmentId(index),
												getCredential(index), fields[i],
												fncs.length == 1 ? fncs[0] : fncs[i])));

						if (logger.isDebugEnabled()) {
							logger.debug(String.format("Use functions %s",
									Stream.of(fncs).map(fnc -> fnc.toString()).collect(Collectors.joining(","))));
						}

						return this;
					}

					@Override
					public WithField<T> publish() {
						int n = getCredentialsAmount();

						IntStream.range(0, n).forEach(index -> Stream.of(fields).forEach(field -> setProperty(type,
								getRole(index), getDepartmentId(index), getCredential(index), field, PUBLISHER)));

						logger.debug("Publish");
						return this;
					}

					@Override
					public WithField<T> mask() {
						int n = getCredentialsAmount();

						IntStream.range(0, n).forEach(index -> Stream.of(fields).forEach(field -> setProperty(type,
								getRole(index), getDepartmentId(index), getCredential(index), field, MASKER)));

						logger.debug("Mask");
						return this;
					}

					@Override
					public WithField<T> anyFields() {
						return owningCredentials.fields(remainingFields.toArray(String[]::new));
					}

					@Override
					public WithCredential<T> roles(Role... roles) {
						return owningType.roles(roles);
					}

					@Override
					public WithCredential<T> departments(UUID... departmentIds) {
						return owningType.departments(departmentIds);
					}

					@Override
					public WithCredential<T> with(Role[] roles, UUID[] departmentIds) {
						return owningType.with(roles, departmentIds);
					}

					@Override
					public WithCredential<T> credentials(Credential... credentials) {
						return owningType.credentials(credentials);
					}

					@Override
					public <E extends DomainEntity> WithType<E> type(Class<E> type) {
						return new WithTypeImpl<>(type);
					}

				}

			}

		}

	}

	public class SecuredPropertyImpl<T extends DomainEntity> implements SecuredProperty<T> {

		private final Class<T> owningType;
		private final String name;
		private final Credential credential;
		private String alias;
		private HandledBiFunction<Arguments<?>, Credential, ?, Exception> function;

		public SecuredPropertyImpl(Class<T> owningType, String name, Credential credential) {
			super();
			this.owningType = owningType;
			this.name = name;
			this.credential = credential;
		}

		@Override
		public Class<T> getOwningType() {
			return owningType;
		}

		@Override
		public Credential getCredential() {
			return credential;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public HandledBiFunction<Arguments<?>, Credential, ?, Exception> getFunction() {
			return function;
		}

		public SecuredPropertyImpl<T> setAlias(String alias) {
			this.alias = alias;
			return this;
		}

		public SecuredPropertyImpl<T> setFunction(HandledBiFunction<Arguments<?>, Credential, ?, Exception> function) {
			this.function = function;
			return this;
		}

		@Override
		public String toString() {
			return "SecuredPropertyImpl[owningType=" + owningType + ", name=" + name + ", credential="
					+ credential.evaluate() + ", alias=" + alias + ", function=" + function + "]";
		}

	}

	private class Key<T extends DomainEntity> {

		private final Class<T> type;
		private final Credential credential;
		private final String name;

		private final int hashCode;

		public Key(Class<T> type, Credential credential, String name) {
			super();
			this.type = requireNonNull(type);
			this.credential = requireNonNull(credential);
			this.name = requireNonNull(name);

			int hash = 17;

			hash += 37 * type.hashCode();
			hash += credential.evaluate().hashCode();
			hash += 37 * name.hashCode();

			hashCode = hash;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;

			Key<?> other = (Key<?>) obj;

			return credential.evaluate().equals(other.credential.evaluate()) && name.equals(other.name)
					&& type.equals(other.type);
		}

		@Override
		public String toString() {
			return "Key[type=" + type + ", credential=" + credential.evaluate() + ", name=" + name + ", hashCode="
					+ hashCode + "]";
		}

	}

}
