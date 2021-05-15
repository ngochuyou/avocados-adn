package adn.service.resource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.connection.LocalStorageConnection;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory;
import adn.service.resource.engine.template.ResourceTemplateImpl;
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
			if (hasIdentifierProperty()) {
				int span = getEntityMetamodel().getPropertySpan() + 1;
				String[] columnNames = new String[span];
				Class<?>[] columnTypes = new Class<?>[span];
				PropertyAccess[] accessors = new PropertyAccess[span];
				Class<?> systemType = getSystemType(getMappedClass());

				columnNames[0] = getIdentifierPropertyName();
				columnTypes[0] = getIdentifierType().getReturnedClass();
				accessors[0] = determinePropertyAccess(systemType, getIdentifierType(), getIdentifierPropertyName());

				int i = 1;

				for (String propertyName : getPropertyNames()) {
					columnNames[i] = propertyName;
					columnTypes[i] = getPropertyType(propertyName).getReturnedClass();
					accessors[i] = determinePropertyAccess(systemType, getPropertyType(propertyName), propertyName);
					i++;
				}

				LocalStorageConnection connection = (LocalStorageConnection) delegate;

				logger.trace(String.format("Registering template [%s]", getEntityName()));
				connection.registerTemplate(
						new ResourceTemplateImpl(getEntityName(), systemType, columnNames, columnTypes, accessors));

				return;
			}

			return;
		}

		throw new IllegalArgumentException(String.format(
				"Unable to register resource template [%s] since connection mismatch, expect connection of type [%s]",
				getEntityName(), LocalStorageConnection.class));
	}

	private Class<?> getSystemType(Class<?> entityType) {
		LocalResource anno = entityType.getDeclaredAnnotation(LocalResource.class);

		Assert.isTrue(anno != null && anno.systemType() != null,
				String.format("[%s] System type must not be null", entityType.getName()));

		return anno.systemType();
	}

	@SuppressWarnings("unchecked")
	private <T, R, E extends Throwable> PropertyAccess determinePropertyAccess(Class<T> systemType, Type type,
			String name) {
		if (type instanceof AbstractExplicitlyExtractedType) {
			AbstractExplicitlyExtractedType<T, R> extractedType = (AbstractExplicitlyExtractedType<T, R>) type;

			if (type instanceof NoOperationSet) {
				return PropertyAccessStrategyFactory.HYBRID_ACCESS_STRATEGY.buildPropertyAccess(null, null,
						extractedType::apply, null);
			}
		}

		return PropertyAccessStrategyFactory.DELEGATE_ACCESS_STRATEGY.buildPropertyAccess(systemType, name);
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
		// TODO Auto-generated method stub
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
