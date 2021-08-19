/**
 * 
 */
package adn.application.context.builders;

import static adn.model.factory.authentication.ModelProducer.MASKER;
import static adn.model.factory.authentication.ModelProducer.PUBLISHER;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import adn.helpers.LoggerHelper;
import adn.model.DomainEntity;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.ModelProducerFactoryBuilder;
import adn.model.factory.authentication.SecuredProperty;
import adn.model.factory.authentication.dynamic.DynamicMapModelProducer;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class DynamicMapModelProducerFactoryImpl implements DynamicMapModelProducerFactory {

	@Override
	public <T extends DomainEntity> DynamicMapModelProducer<T> getProducers(Class<T> entityType) {
		// TODO Auto-generated method stub
		return null;
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

			return new Key<>(type, CredentialFactory.from(role, departmentId), name);
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

		private void modifyFunction(SecuredPropertyImpl<?> property, BiFunction<Object, Credential, Object> newFnc) {
			logger.debug(String.format("Overring function [%s] with [%s]", property.getFunction(), newFnc));
			property.setFunction(newFnc);
		}

		private <T extends DomainEntity> void setProperty(Class<T> type, Role role, UUID departmentId,
				Credential credential, String name, BiFunction<Object, Credential, Object> fnc) {
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
			}

			@Override
			public WithCredential<T> role(Role role) {
				return new WithCredentialImpl(role);
			}

			@Override
			public WithCredential<T> department(UUID departmentId) {
				return new WithCredentialImpl(departmentId);
			}

			@Override
			public WithCredential<T> with(Role role, UUID departmentId) {
				return new WithCredentialImpl(role, departmentId);
			}

			@Override
			public WithCredential<T> credential(Credential credential) {
				return new WithCredentialImpl(credential);
			}

			private class WithCredentialImpl implements WithCredential<T> {

				private final Set<String> remainingFields = modelContext.getMetadata(type).getPropertyNames();

				private Role role;
				private UUID departmentId;
				private Credential credential;

				public WithCredentialImpl(Role role) {
					super();
					this.role = role;
					log(role);
				}

				public WithCredentialImpl(UUID departmentId) {
					super();
					this.departmentId = departmentId;
					log(departmentId);
				}

				public WithCredentialImpl(Role role, UUID departmentId) {
					super();
					this.role = role;
					this.departmentId = departmentId;
					log(role, departmentId);
				}

				public WithCredentialImpl(Credential credential) {
					super();
					this.credential = credential;
					log(credential);
				}

				@Override
				public WithCredential<T> role(Role role) {
					this.role = role;
					log(role);
					return this;
				}

				@Override
				public WithCredential<T> department(UUID departmentId) {
					this.departmentId = departmentId;
					log(departmentId);
					return this;
				}

				@Override
				public WithCredential<T> with(Role role, UUID departmentId) {
					this.role = role;
					this.departmentId = departmentId;
					log(role, departmentId);
					return this;
				}

				@Override
				public WithCredential<T> credential(Credential credential) {
					this.credential = credential;
					log(credential);
					return this;
				}

				private void log(Role role) {
					logger.debug(String.format("With role[%s]", role));
				}

				private void log(UUID departmentId) {
					logger.debug(String.format("With departmentId[%s]", departmentId));
				}

				private void log(Role role, UUID departmentId) {
					logger.debug(String.format("With role[%s] and departmentId[%s]", role, departmentId));
				}

				private void log(Credential credential) {
					logger.debug(String.format("With credential[%s]", credential.evaluate()));
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

				@Override
				public WithCredential<T> mask() {
					modelContext.getMetadata(type).getPropertyNames()
							.forEach(propName -> setProperty(type, role, departmentId, credential, propName, MASKER));
					logger.debug("Mask all");
					return this;
				}

				@Override
				public WithCredential<T> publish() {
					modelContext.getMetadata(type).getPropertyNames().forEach(
							propName -> setProperty(type, role, departmentId, credential, propName, PUBLISHER));
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
					public WithField<T> use(String... alias) {
						if (requireNonNull(alias).length != fields.length) {
							throw new IllegalArgumentException(String.format(
									"Alias names length and field lengths must match. Alias[%d]><[%d]Fields",
									alias.length, fields.length));
						}

						IntStream.range(0, fields.length).forEach(index -> setProperty(type, role, departmentId,
								credential, fields[index], alias[index]));
						logger.debug(
								String.format("Using alias %s", Stream.of(alias).collect(Collectors.joining(","))));

						return this;
					}

					@Override
					public WithField<T> use(BiFunction<Object, Credential, Object>[] fncs) {
						if (requireNonNull(fncs).length != fields.length) {
							throw new IllegalArgumentException(String.format(
									"Functions length and field lengths must match. Functions[%d]><[%d]Fields",
									fncs.length, fields.length));
						}

						IntStream.range(0, fields.length).forEach(
								index -> setProperty(type, role, departmentId, credential, fields[index], fncs[index]));

						if (logger.isDebugEnabled()) {
							logger.debug(String.format("Use functions %s",
									Stream.of(fncs).map(fnc -> fnc.toString()).collect(Collectors.joining(","))));
						}

						return this;
					}

					@Override
					public WithField<T> publish() {
						Stream.of(fields)
								.forEach(field -> setProperty(type, role, departmentId, credential, field, PUBLISHER));
						logger.debug("Publish");
						return this;
					}

					@Override
					public WithField<T> mask() {
						Stream.of(fields)
								.forEach(field -> setProperty(type, role, departmentId, credential, field, MASKER));
						logger.debug("Mask");
						return this;
					}

					@Override
					public WithField<T> anyFields() {
						return owningCredentials.fields(remainingFields.toArray(String[]::new));
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
		private BiFunction<Object, Credential, Object> function;

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
		public BiFunction<Object, Credential, Object> getFunction() {
			return function;
		}

		public SecuredPropertyImpl<T> setAlias(String alias) {
			this.alias = alias;
			return this;
		}

		public SecuredPropertyImpl<T> setFunction(BiFunction<Object, Credential, Object> function) {
			this.function = function;
			return this;
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
