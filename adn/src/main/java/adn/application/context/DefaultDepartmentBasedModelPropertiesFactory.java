/**
 * 
 */
package adn.application.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.util.Assert;

import adn.application.Constants;
import adn.helpers.TypeHelper;
import adn.model.DepartmentScoped;
import adn.model.DomainEntity;
import adn.model.ModelContextProvider;
import adn.model.entities.metadata.EntityMetadata;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.model.factory.property.production.DepartmentBasedModelPropertiesProducer;
import adn.model.factory.property.production.DepartmentScopedProperty;
import adn.model.factory.property.production.ModelPropertiesProducer;
import adn.model.factory.property.production.department.DepartmentBasedModelPropertiesProducerImpl;
import adn.model.factory.property.production.department.DepartmentBasedModelPropertiesProducersBuilder;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(9)
public class DefaultDepartmentBasedModelPropertiesFactory
		implements DepartmentBasedModelPropertiesFactory, ContextBuilder {

	private Map<Class<? extends DepartmentScoped>, DepartmentBasedModelPropertiesProducer> producersMap;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info(String.format("%s %s", ContextBuilder.super.getLoggingPrefix(this),
				String.format("Building %s", this.getClass().getSimpleName())));

		ModelContextProvider modelContext;
		final DepartmentBasedModelPropertiesProducersBuilderImpl builder = new DepartmentBasedModelPropertiesProducersBuilderImpl(
				modelContext = ContextProvider.getBean(ModelContextProvider.class));
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(
				new AssignableTypeFilter(DepartmentBasedModelPropertiesProducersBuilderContributor.class));

		DepartmentBasedModelPropertiesProducersBuilderContributor contributor;

		for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.ROOT_PACKAGE)) {
			try {
				Class<? extends DepartmentBasedModelPropertiesProducersBuilderContributor> contributorClass = (Class<? extends DepartmentBasedModelPropertiesProducersBuilderContributor>) Class
						.forName(beanDef.getBeanClassName());

				logger.info(String.format("Found one %s of type [%s]",
						DepartmentBasedModelPropertiesProducersBuilderContributor.class.getSimpleName(),
						beanDef.getBeanClassName()));

				contributor = contributorClass.getConstructor().newInstance();
				contributor.contribute(builder);
			} catch (NoSuchMethodException nsm) {
				SpringApplication.exit(ContextProvider.getApplicationContext());
				throw new IllegalArgumentException(
						String.format("A non-arg constructor is required on a %s instance, unable to find one in [%s]",
								DepartmentBasedModelPropertiesProducersBuilderContributor.class.getSimpleName(),
								beanDef.getBeanClassName()));
			} catch (Exception any) {
				any.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		}

		producersMap = new HashMap<>(0, 1f);

		modelContext.getEntityTree().forEach(branch -> {
			if (!DepartmentScoped.class.isAssignableFrom(branch.getNode())) {
				return;
			}

			Class<DepartmentScoped> entityType = (Class<DepartmentScoped>) branch.getNode();

			producersMap.put(entityType,
					new DepartmentBasedModelPropertiesProducerImpl(entityType,
							builder.propertiesMap.values().stream()
									.filter(prop -> TypeHelper.isParentOf(entityType, prop.getEntityType()))
									.map(prop -> (DepartmentScopedProperty<DepartmentScoped>) prop)
									.collect(Collectors.toSet())));
		});

		logger.info(String.format("%s %s", ContextBuilder.super.getLoggingPrefix(this),
				String.format("Finished building %s", this.getClass().getSimpleName())));
	}

	private <T extends DepartmentScoped> DepartmentBasedModelPropertiesProducer getProducer(Class<T> type) {
		return producersMap.get(type);
	}

	@Override
	public <T extends DepartmentScoped> Map<String, Object> produce(Class<T> type, Object[] source, String[] columns,
			UUID departmentId) {
		DepartmentBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(source, columns, departmentId);
	}

	@Override
	public <T extends DepartmentScoped> List<Map<String, Object>> produce(Class<T> type, List<Object[]> sources,
			String[] columns, UUID departmentId) {
		DepartmentBasedModelPropertiesProducer producer = getProducer(type);

		return producer.produce(sources, columns, departmentId);
	}

	@Override
	public <T extends DepartmentScoped> Map<String, Object> singularProduce(Class<T> type, Object source, String column,
			UUID departmentId) {
		DepartmentBasedModelPropertiesProducer producer = getProducer(type);

		return producer.singularProduce(source, column, departmentId);
	}

	@Override
	public <T extends DepartmentScoped> List<Map<String, Object>> singularProduce(Class<T> type, List<Object> sources,
			String column, UUID departmentId) {
		DepartmentBasedModelPropertiesProducer producer = getProducer(type);

		return producer.singularProduce(sources, column, departmentId);
	}

	@Override
	public <T extends DepartmentScoped> Collection<String> validateColumnNames(Class<T> type,
			Collection<String> requestedColumns) throws NoSuchFieldException {
		DepartmentBasedModelPropertiesProducer producer = getProducer(type);

		return producer.validateAndTranslateColumnNames(requestedColumns);
	}

	public class DepartmentBasedModelPropertiesProducersBuilderImpl
			implements DepartmentBasedModelPropertiesProducersBuilder {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		private final Map<Key<? extends DepartmentScoped>, DepartmentScopedProperty<? extends DepartmentScoped>> propertiesMap = new HashMap<>();
		private final ModelContextProvider modelContext;

		public DepartmentBasedModelPropertiesProducersBuilderImpl(ModelContextProvider modelContext) {
			this.modelContext = modelContext;
		}

		@SuppressWarnings("unchecked")
		private <T extends DepartmentScoped> EntityMetadata getMetadata(Class<T> type) {
			return modelContext.getMetadata((Class<DomainEntity>) type);
		}

		@Override
		public <T extends DepartmentScoped> WithType<T> type(Class<T> type) {
			return new WithTypeImpl<>(this, type);
		}

		private Function<Object, Object> masker() {
			return ModelPropertiesProducer.MASKER;
		}

		private Function<Object, Object> publisher() {
			return ModelPropertiesProducer.PUBLISHER;
		}

		public abstract class AbstractOwned implements Owned {

			protected final DepartmentBasedModelPropertiesProducersBuilder owner;

			public AbstractOwned(DepartmentBasedModelPropertiesProducersBuilder owner) {
				this.owner = owner;
			}

			@Override
			public DepartmentBasedModelPropertiesProducersBuilder and() {
				return owner;
			}

		}

		public class WithTypeImpl<T extends DepartmentScoped> extends AbstractOwned implements WithType<T> {

			private final Class<T> type;

			public WithTypeImpl(DepartmentBasedModelPropertiesProducersBuilder owner, Class<T> type) {
				super(owner);
				this.type = type;
				logger.trace(String.format("With [%s]", type.getName()));
			}

			@Override
			public WithDepartment department(UUID departmentId, String loggableName) {
				return new WithDepartmentImpl(owner, this, departmentId, loggableName);
			}

			public class WithDepartmentImpl extends AbstractOwned implements WithDepartment {

				private final UUID departmentId;
				private final WithType<T> owningType;

				public WithDepartmentImpl(DepartmentBasedModelPropertiesProducersBuilder owner, WithType<T> owningType,
						UUID departmentId, String loggableName) {
					super(owner);
					this.departmentId = departmentId;
					this.owningType = owningType;
					logger.trace(String.format("With department id [%s] named [%s]", departmentId,
							Optional.ofNullable(loggableName).orElse("UNKNOWN")));
				}

				@Override
				public WithField fields(String... fieldNames) {
					return new WithFieldImpl(owner, this, fieldNames);
				}

				private WithDepartment apply(Function<Object, Object> fnc) {
					getMetadata(type).getPropertyNames().forEach(name -> {
						propertiesMap.put(new Key<>(type, departmentId, name),
								new ScopedPropertyImpl<>(type, name, departmentId, fnc));
					});

					return this;
				}

				@Override
				public WithDepartment mask() {
					logger.trace("Mask all");
					return apply(masker());
				}

				@Override
				public WithDepartment publish() {
					logger.trace("Publish all");
					return apply(publisher());
				}

				@Override
				public WithDepartment department(UUID departmentId, String loggableName) {
					return owningType.department(departmentId, loggableName);
				}

				public class WithFieldImpl extends AbstractOwned implements WithField {

					private final String[] names;
					private final WithDepartment owningDepartment;

					public WithFieldImpl(DepartmentBasedModelPropertiesProducersBuilder owner,
							WithDepartment owningDepartment, String[] name) {
						super(owner);
						this.names = name;
						this.owningDepartment = owningDepartment;
						logger.trace(
								String.format("With fields [%s]", Stream.of(name).collect(Collectors.joining(", "))));
					}

					private WithField apply(Function<Object, Object> fnc) {
						Stream.of(names).forEach(name -> {
							propertiesMap.put(new Key<>(type, departmentId, name),
									new ScopedPropertyImpl<>(type, name, departmentId, fnc));
						});

						return this;
					}

					@SuppressWarnings("unchecked")
					@Override
					public <F, R> WithField use(Function<F, R> fnc) {
						logger.trace(String.format("Use %s", fnc));
						return apply((Function<Object, Object>) fnc);
					}

					@Override
					public WithField mask() {
						logger.trace("Mask all");
						return apply(masker());
					}

					@Override
					public WithField publish() {
						logger.trace("Publish all");
						return apply(publisher());
					}

					@Override
					public WithField fields(String... fieldNames) {
						return owningDepartment.fields(fieldNames);
					}

					@Override
					public WithDepartment department(UUID departmentId, String loggableName) {
						return owningType.department(departmentId, loggableName);
					}

					@Override
					public WithField others() {
						return owningDepartment.fields(getMetadata(type).getPropertyNames().stream()
								.filter(propName -> Stream.of(names)
										.filter(requestedName -> propName.equals(requestedName)).count() == 0)
								.toArray(String[]::new));
					}

				}

			}

		}

	}

	public class ScopedPropertyImpl<T extends DepartmentScoped> implements DepartmentScopedProperty<T> {
		private final Class<T> entityType;
		private final String name;
		private final UUID departmentId;
		private final Function<Object, Object> fnc;

		public ScopedPropertyImpl(Class<T> entityType, String name, UUID departmentId, Function<Object, Object> fnc) {
			super();
			Assert.notNull(entityType, "Entity type must not be null");
			Assert.notNull(name, "Prperty name must not be null");
			Assert.notNull(departmentId, "Department ID must not be null");
			Assert.notNull(fnc, "Function must not be null");
			this.entityType = entityType;
			this.name = name;
			this.departmentId = departmentId;
			this.fnc = fnc;
		}

		@Override
		public Class<T> getEntityType() {
			return entityType;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public UUID getDepartmentId() {
			return departmentId;
		}

		@Override
		public Function<Object, Object> getFunction() {
			return fnc;
		}

	}

	private class Key<T extends DepartmentScoped> {

		private final Class<T> type;
		private final UUID departmentId;
		private final String originalName;

		private final int hashCode;

		private Key(Class<T> type, UUID departmentId, String originalName) {
			Assert.notNull(type, "Null type");
			Assert.notNull(departmentId, "Null department id");
			Assert.notNull(originalName, "Null name");
			this.type = type;
			this.departmentId = departmentId;
			this.originalName = originalName;

			int hash = 17;

			hash += 37 * type.hashCode();
			hash += (departmentId != null ? 37 * departmentId.hashCode() : 0);
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

			if (this.departmentId == null && that.departmentId == null) {
				return this.type.equals(that.type) && this.originalName.equals(originalName);
			}

			return this.type.equals(that.type)
					&& (that.departmentId == null ? this.departmentId.equals(that.departmentId)
							: that.departmentId.equals(this.departmentId))
					&& this.originalName.equals(originalName);
		}

	}

	interface DepartmentBasedModelPropertiesProducersBuilderContributor {

		void contribute(DepartmentBasedModelPropertiesProducersBuilder builder);

	}

}
