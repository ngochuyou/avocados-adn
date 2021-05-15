package adn.service.resource;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.GeneratedValue;
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
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.connection.LocalStorageConnection;
import adn.service.resource.factory.EntityManagerFactoryImplementor;
import adn.service.resource.factory.EntityPersisterImplementor;

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
		logger.trace(String.format("Registering template [%s]", getEntityName()));

		SessionCreationOptions sessionCreationOptions = ResourceSession.getSessionCreationOptions();
		Connection delegate = sessionCreationOptions.getConnection();

		if (delegate instanceof LocalStorageConnection) {
			try {
				int span = getEntityMetamodel().getPropertySpan();
				String[] columnNames = new String[span];
				Class<?>[] columnTypes = new Class<?>[span];
				PropertyAccess[] accessors = new PropertyAccess[span];

				if (hasIdentifierProperty()) {
					columnNames[0] = getIdentifierPropertyName();
					columnTypes[0] = getIdentifierType().getReturnedClass();
					accessors[0] = determinePropertyAccess(getIdentifierPropertyName());
				}

				LocalStorageConnection connection = delegate.unwrap(LocalStorageConnection.class);
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	private PropertyAccess determinePropertyAccess(String name) {
		EntityTypeDescriptor<D> descriptor = getFactory().getMetamodel().entity(getEntityName());
		Attribute<? super D, ?> idAttribute = descriptor.getAttribute(name);

		if (!(idAttribute.getJavaMember() instanceof Field)) {
			throw new IllegalArgumentException(
					String.format("Unable to locate [%s] for property named [%s]", PropertyAccess.class, name));
		}

		Field field = (Field) idAttribute.getJavaMember();

		if (field.isAnnotationPresent(GeneratedValue.class)) {
			return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyAccess getPropertyAccess(int propertyIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable loadEntityIdByNaturalId(Object[] naturalIdValues, LockOptions lockOptions,
			SharedSessionContractImplementor session) {
		Object instance = getFactory().getStorage().select((Serializable) naturalIdValues[0]);

		if (instance == null) {
			return null;
		}

		return (Serializable) getIdentifierType().resolve(getIdentifier(instance), session, instance);
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
