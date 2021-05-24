package adn.service.resource;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.persistence.metamodel.Attribute;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.SessionCreationOptions;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;
import adn.helpers.StringHelper;
import adn.service.resource.connection.LocalStorageConnection;
import adn.service.resource.engine.access.DirectAccess;
import adn.service.resource.engine.access.LiterallyNamedAccess;
import adn.service.resource.engine.access.PropertyAccessDelegate;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess.LambdaType;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.access.StandardAccess;
import adn.service.resource.engine.template.ResourceTemplateImpl;
import adn.service.resource.engine.tuple.InstantiatorFactory;
import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;
import adn.service.resource.factory.EntityManagerFactoryImplementor;
import adn.service.resource.factory.EntityPersisterImplementor;
import adn.service.resource.type.AbstractExplicitlyExtractedType;
import adn.service.resource.type.NoOperationSet;

/**
 * @author Ngoc Huy
 *
 * @param <D>
 */
public class ResourcePersisterImpl<D> extends SingleTableEntityPersister
		implements EntityPersisterImplementor<D>, ClassMetadata, SharedSessionUnwrapper, SessionFactoryObserver {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final UniqueEntityLoader resourceLoader;

	public ResourcePersisterImpl(PersistentClass persistentClass, EntityDataAccess cacheAccessStrategy,
			NaturalIdDataAccess naturalIdRegionAccessStrategy, PersisterCreationContext creationContext)
			throws HibernateException, IllegalAccessException, NoSuchFieldException, SecurityException {
		super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);

		creationContext.getSessionFactory().addObserver(this);
		resourceLoader = new ResourceLoader(this);
	}

	@Override
	public void sessionFactoryCreated(SessionFactory factory) {
		SessionCreationOptions sessionCreationOptions = ResourceSession.getSessionCreationOptions();
		Connection delegate = sessionCreationOptions.getConnection();

		if (delegate instanceof LocalStorageConnection) {
			int span = getEntityMetamodel().getPropertySpan() + 1;
			String[] columnNames = new String[span];
			Class<?>[] columnTypes = new Class<?>[span];
			ResourceInstantiator<File> instantiator = determineInstantiator();
			PropertyAccessImplementor[] accessors = new PropertyAccessImplementor[span];

			columnNames[0] = getIdentifierColumnNames()[0];
			columnTypes[0] = getIdentifierType().getReturnedClass();
			accessors[0] = determinePropertyAccess(getIdentifierType(), getIdentifierPropertyName());

			String propertyName;
			String propertyColumnName;

			for (int i = 0, j = 1; i < span - 1; i++, j++) {
				propertyName = getPropertyNames()[i];
				propertyColumnName = getPropertyColumnNames(i)[0];

				columnNames[j] = StringHelper.hasLength(propertyColumnName) ? propertyColumnName : propertyName;
				columnTypes[j] = getPropertyType(propertyName).getReturnedClass();
				accessors[j] = determinePropertyAccess(getPropertyType(propertyName), propertyName);
			}

			LocalStorageConnection connection = (LocalStorageConnection) delegate;

			logger.trace(String.format("Registering template [%s]", getEntityName()));
			connection.registerTemplate(
					new ResourceTemplateImpl(String.format("%s#%s", getTableName(), getDiscriminatorValue()),
							columnNames[0], columnNames, columnTypes, accessors, instantiator));

			return;
		}

		throw new IllegalArgumentException(String.format(
				"Unable to register resource template [%s] since connection mismatch, expect connection of type [%s]",
				getEntityName(), LocalStorageConnection.class));
	}

	@SuppressWarnings("unchecked")
	private ResourceInstantiator<File> determineInstantiator() {
		LocalResource anno = (LocalResource) getMappedClass().getDeclaredAnnotation(LocalResource.class);

		if (anno.constructorParameterColumnNames().length == 0) {
			return InstantiatorFactory.buildDefault(File.class);
		}

		return InstantiatorFactory.build(File.class, anno.constructorParameterColumnNames(),
				anno.constructorParameterTypes());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <F, S, R, E extends RuntimeException> PropertyAccessImplementor determinePropertyAccess(Type propertyType,
			String propertyName) {
		org.springframework.data.annotation.AccessType.Type accessTypeAnno = locateAnnotatedAccessType(propertyName);
		if (accessTypeAnno != org.springframework.data.annotation.AccessType.Type.PROPERTY
				&& !(propertyType instanceof AbstractExplicitlyExtractedType)) {
			// @formatter:off
			Getter getter = Optional.of(StandardAccess.locateGetter(File.class, propertyName)
										.orElse(DirectAccess.locateGetter(File.class, propertyName)
												.orElse(LiterallyNamedAccess.locateGetter(File.class, propertyName)
														.orElse(null))))
									.orElseThrow(() -> new IllegalArgumentException(String.format("Unable to locate getter for property [%s]", propertyName)));
			Setter setter = Optional.of(StandardAccess.locateSetter(File.class, propertyName)
										.orElse(DirectAccess.locateSetter(File.class, propertyName)
												.orElse(!(getter instanceof LiterallyNamedAccess) ? LiterallyNamedAccess.locateSetter(File.class, propertyName)
														.orElse(null) : null)))
									.orElseThrow(() -> new IllegalArgumentException(String.format("Unable to locate setter for property [%s]", propertyName)));
			// @formatter:on
			return PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY.buildPropertyAccess(getter, setter);
		}

		Setter setter;

		setter = propertyType instanceof NoOperationSet ? null
				: PropertyAccessDelegate.locateSetter(File.class, propertyName, false, propertyType.getReturnedClass()).orElse(null);

		if (!(propertyType instanceof AbstractExplicitlyExtractedType)) {
			Getter getter = PropertyAccessDelegate.locateGetter(File.class, propertyName, false).orElse(null);

			return PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY.buildPropertyAccess(getter, setter);
		}

		LambdaType getterLambdaType = determineLambdaType(propertyType);
		LambdaType setterLambdaType = determineLambdaType(propertyType);

		if (setter == null) {
			if (getterLambdaType == LambdaType.NO_ACCESS && setterLambdaType == LambdaType.NO_ACCESS) {
				throw new IllegalArgumentException(String
						.format("Unable to locate access for property [%s] in type [%s]", propertyName, File.class));
			}

			if (getterLambdaType != setterLambdaType) {
				return PropertyAccessStrategyFactory.createMixedAccess().buildPropertyAccess(getterLambdaType,
						setterLambdaType);
			}

			if (getterLambdaType == LambdaType.FUNCTION) {
				return PropertyAccessStrategyFactory.createFunctionalAccess()
						.buildPropertyAccess((HandledFunction) propertyType, (HandledFunction) propertyType);
			}

			if (getterLambdaType == LambdaType.BIFUNCTION) {
				return PropertyAccessStrategyFactory.createBiFunctionalAccess()
						.buildPropertyAccess((HandledBiFunction) propertyType, (HandledBiFunction) propertyType);
			}

			if (getterLambdaType == LambdaType.CONSUMER) {
				return PropertyAccessStrategyFactory.createConsumingAccess()
						.buildPropertyAccess((HandledConsumer) propertyType, (HandledConsumer) propertyType);
			}

			return PropertyAccessStrategyFactory.createSupplyingAccess()
					.buildPropertyAccess((HandledSupplier) propertyType, (HandledSupplier) propertyType);
		}
		// @formatter:off
		return PropertyAccessStrategyFactory.createHybridAccess().buildPropertyAccess(
					null,
					setter,
					getterLambdaType == LambdaType.NO_ACCESS ? null : propertyType,
					setterLambdaType == LambdaType.NO_ACCESS ? null : propertyType);
		// @formatter:on
	}

	private LambdaType determineLambdaType(Type type) {
		if (type instanceof HandledFunction) {
			return LambdaType.FUNCTION;
		}

		if (type instanceof HandledBiFunction) {
			return LambdaType.BIFUNCTION;
		}

		if (type instanceof HandledConsumer) {
			return LambdaType.CONSUMER;
		}

		if (type instanceof HandledSupplier) {
			return LambdaType.SUPPLIER;
		}

		return LambdaType.NO_ACCESS;
	}

	private org.springframework.data.annotation.AccessType.Type locateAnnotatedAccessType(String name) {
		Attribute<? super D, ?> attribute = getFactory().getMetamodel().entity(getEntityName()).getAttribute(name);
		Field member;

		if ((member = (Field) attribute.getJavaMember())
				.isAnnotationPresent(org.springframework.data.annotation.AccessType.class)) {
			return member.getDeclaredAnnotation(org.springframework.data.annotation.AccessType.class).value();
		}

		return null;
	}

	@Override
	public EntityManagerFactoryImplementor getFactory() {
		return (EntityManagerFactoryImplementor) super.getFactory();
	}

	@Override
	public ResourcePersister<D> getEntityPersister() {
		return this;
	}

	@Override
	public PropertyAccess getPropertyAccess(String propertyName) {
		return null;
	}

	@Override
	public PropertyAccess getPropertyAccess(int propertyIndex) {
		return null;
	}

	@Override
	public Object load(Serializable id, Object optionalObject, LockOptions lockOptions,
			SharedSessionContractImplementor session) throws HibernateException {
		return resourceLoader.load(id, optionalObject, session, lockOptions);
	}

	@Override
	public Object load(Serializable id, Object optionalObject, LockOptions lockOptions,
			SharedSessionContractImplementor session, Boolean readOnly) throws HibernateException {
		return resourceLoader.load(id, optionalObject, session, lockOptions, readOnly);
	}

	@Override
	public Object[] hydrate(ResultSet rs, Serializable id, Object object, Loadable rootLoadable,
			String[][] suffixedPropertyColumns, boolean allProperties, SharedSessionContractImplementor session)
			throws SQLException, HibernateException {
		logger.trace(String.format("[Row-Hydrate: %s]", id.toString()));

		Type[] propertyTypes = getPropertyTypes();
		int n = propertyTypes.length;
		Object[] values = new Object[n];

		for (int i = 0; i < n; i++) {
			values[i] = propertyTypes[i].hydrate(rs, getPropertyColumnNames(i), session, object);
		}

		return values;
	}

}
