package adn.service.resource;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import org.springframework.util.Assert;

import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.StringHelper;
import adn.service.resource.connection.LocalStorageConnection;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.FunctionalPropertyAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessDelegate;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void sessionFactoryCreated(SessionFactory factory) {
		SessionCreationOptions sessionCreationOptions = ResourceSession.getSessionCreationOptions();
		Connection delegate = sessionCreationOptions.getConnection();

		if (delegate instanceof LocalStorageConnection) {
			int span = getEntityMetamodel().getPropertySpan() + 1;
			Class<?> systemType = getSystemType(getMappedClass());
			String[] columnNames = new String[span];
			Class<?>[] columnTypes = new Class<?>[span];
			ResourceInstantiator<?> instantiator = determineInstantiator(systemType);
			PropertyAccessDelegate[] accessors = new PropertyAccessDelegate[span];

			columnNames[0] = getIdentifierColumnNames()[0];
			columnTypes[0] = getIdentifierType().getReturnedClass();
			accessors[0] = determinePropertyAccess(systemType, getIdentifierType(), getIdentifierPropertyName());

			String propertyName;
			String propertyColumnName;

			for (int i = 0, j = 1; i < span - 1; i++, j++) {
				propertyName = getPropertyNames()[i];
				propertyColumnName = getPropertyColumnNames(i)[0];

				columnNames[j] = StringHelper.hasLength(propertyColumnName) ? propertyColumnName : propertyName;
				columnTypes[j] = getPropertyType(propertyName).getReturnedClass();
				accessors[j] = determinePropertyAccess(systemType, getPropertyType(propertyName), propertyName);
			}

			LocalStorageConnection connection = (LocalStorageConnection) delegate;

			logger.trace(String.format("Registering template [%s]", getEntityName()));
			connection.registerTemplate(
					new ResourceTemplateImpl(String.format("%s#%s", getTableName(), getDiscriminatorValue()),
							systemType, columnNames, columnTypes, accessors, instantiator));

			return;
		}

		throw new IllegalArgumentException(String.format(
				"Unable to register resource template [%s] since connection mismatch, expect connection of type [%s]",
				getEntityName(), LocalStorageConnection.class));
	}

	@SuppressWarnings("unchecked")
	private <T> ResourceInstantiator<T> determineInstantiator(Class<T> systemType) {
		LocalResource anno = (LocalResource) getMappedClass().getDeclaredAnnotation(LocalResource.class);

		if (anno.columnNames().length == 0) {
			return InstantiatorFactory.buildDefault(systemType);
		}

		return InstantiatorFactory.build(systemType, anno.columnNames(), anno.constructorParameterTypes());
	}

	private Class<?> getSystemType(Class<?> entityType) {
		LocalResource anno = entityType.getDeclaredAnnotation(LocalResource.class);

		Assert.isTrue(anno != null && anno.systemType() != null,
				String.format("[%s] System type must not be null", entityType.getName()));

		return anno.systemType();
	}

	@SuppressWarnings("unchecked")
	private <T, R, E extends Throwable> PropertyAccessDelegate determinePropertyAccess(Class<T> systemType, Type type,
			String name) {
		Object getter = determineGetter(systemType, type, name);
		Object setter = determineSetter(systemType, type, name);

		switch (AccessType.determineAccessType(getter, setter)) {
			case STANDARD: {
				return (PropertyAccessDelegate) PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY
						.buildPropertyAccess((Getter) getter, (Setter) setter);
			}

			case FUNCTIONAL: {
				return PropertyAccessStrategyFactory.FUNCTIONAL_ACCESS_STRATEGY
						.buildPropertyAccess((HandledFunction<T, R, E>) getter, (HandledFunction<T, R, E>) setter);
			}

			case HYBRID: {
				if (getter instanceof Getter) {
					return PropertyAccessStrategyFactory.HYBRID_ACCESS_STRATEGY.buildPropertyAccess((Getter) getter,
							null, null, (HandledFunction<T, R, E>) setter);
				}

				return PropertyAccessStrategyFactory.HYBRID_ACCESS_STRATEGY.buildPropertyAccess(null, (Setter) setter,
						(HandledFunction<T, R, E>) getter, null);
			}
		}

		return (PropertyAccessDelegate) PropertyAccessStrategyFactory.DELEGATE_ACCESS_STRATEGY
				.buildPropertyAccess(systemType, name);
	}

	private enum AccessType {

		/**
		 * Determines that we use {@link Getter} and {@link Setter} only
		 */
		STANDARD,

		/**
		 * Determines that we use {@link HandledFunction} getter and
		 * {@link HandledFunction} setter only
		 */
		FUNCTIONAL,

		/**
		 * A mix of those two other types. However, there must only be one instance of
		 * each.
		 * <p>
		 * If there's a {@link Getter} then the <i>{@link HandledFunction} Getter</i>
		 * must be null and vice versa
		 */
		HYBRID;

		private static AccessType determineAccessType(Object getter, Object setter) {
			if (getter instanceof Getter && setter instanceof Setter) {
				return STANDARD;
			}

			if (getter instanceof FunctionalPropertyAccess && setter instanceof FunctionalPropertyAccess) {
				return FUNCTIONAL;
			}

			return HYBRID;
		}

	}

	private <T, R> Object determineSetter(Class<?> systemType, Type type, String name) {
		if (type instanceof NoOperationSet) {
			return null;
		}

		if (locateAnnotatedAccessType(name) == org.springframework.data.annotation.AccessType.Type.PROPERTY) {
			// we've now known that the access to this field will be executed as a method,
			// therefore,
			// the factory can locate setter without checking field existence
			return PropertyAccessStrategyFactory.locateSetter(systemType, name, false, type.getReturnedClass());
		}

		return PropertyAccessStrategyFactory.locateSetter(systemType, name, true);
	}

	@SuppressWarnings("unchecked")
	private <T, R> Object determineGetter(Class<?> systemType, Type type, String name) {
		if (type instanceof AbstractExplicitlyExtractedType) {
			AbstractExplicitlyExtractedType<T, R> extractedType = (AbstractExplicitlyExtractedType<T, R>) type;

			return extractedType;
		}

		if (locateAnnotatedAccessType(name) == org.springframework.data.annotation.AccessType.Type.PROPERTY) {
			// we've now known that the access to this field will be executed as a method,
			// therefore,
			// the factory can locate getter without checking field existence
			return PropertyAccessStrategyFactory.locateGetter(systemType, name, false);
		}

		return PropertyAccessStrategyFactory.locateGetter(systemType, name, true);
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
