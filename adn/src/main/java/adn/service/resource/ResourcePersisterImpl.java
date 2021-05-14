package adn.service.resource;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.factory.EntityManagerFactoryImplementor;
import adn.service.resource.factory.EntityPersisterImplementor;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;
import adn.service.resource.type.AbstractSyntheticBasicType;
import adn.service.resource.type.ExplicitlyHydratedType;

/**
 * @author Ngoc Huy
 *
 * @param <D>
 */
public class ResourcePersisterImpl<D> extends SingleTableEntityPersister
		implements EntityPersisterImplementor<D>, ClassMetadata, SharedSessionUnwrapper {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final UniqueEntityLoader resourceLoader;

	public ResourcePersisterImpl(PersistentClass persistentClass, EntityDataAccess cacheAccessStrategy,
			NaturalIdDataAccess naturalIdRegionAccessStrategy, PersisterCreationContext creationContext)
			throws HibernateException, IllegalAccessException, NoSuchFieldException, SecurityException {
		super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);

		@SuppressWarnings("unchecked")
		Iterator<Property> declaredPropertyIterator = persistentClass.getDeclaredPropertyIterator();
		ResultSetMetaDataImplementor rsMetadata = creationContext.getSessionFactory().getServiceRegistry()
				.getService(ResultSetMetaDataImplementor.class);

		if (persistentClass.hasIdentifierProperty()) {
			addResultSetMetadataColumn(rsMetadata, persistentClass.getIdentifierProperty().getType(),
					persistentClass.getIdentifierProperty().getName());
		}

		while (declaredPropertyIterator.hasNext()) {
			Property prop = (Property) declaredPropertyIterator.next();

			addResultSetMetadataColumn(rsMetadata, prop.getValue().getType(), prop.getName());
		}

		resourceLoader = new ResourceLoader(this);
	}

	private void addResultSetMetadataColumn(ResultSetMetaDataImplementor rsMetadata, Type propertyType,
			String propertyName) throws IllegalAccessException, NoSuchFieldException, SecurityException {
		if (propertyType instanceof AbstractSyntheticBasicType) {
			if (propertyType instanceof ExplicitlyHydratedType) {
				rsMetadata.getAccess().addExplicitlyHydratedColumn(propertyName);
				return;
			}

			rsMetadata.getAccess().addSynthesizedColumn(propertyName);
			return;
		}

		rsMetadata.getAccess().addColumn(propertyName);
		return;
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

	@Override
	public int dehydrate(Serializable id, Object[] fields, Object rowId, boolean[] includeProperty,
			boolean[][] includeColumns, int j, PreparedStatement ps, SharedSessionContractImplementor session,
			int index, boolean isUpdate) throws SQLException, HibernateException {
		logger.trace("[Entity-Dehydrate: %s]", id.toString());

		return super.dehydrate(id, fields, rowId, includeProperty, includeColumns, j, ps, session, index, isUpdate);
	}

	@Override
	public void insert(Serializable id, Object[] fields, Object object, SharedSessionContractImplementor session) {
		preInsertInMemoryValueGeneration(fields, object, session);

		insert(id, fields, deleteCallable, batchSize, ROWID_ALIAS, object, session);

		super.insert(id, fields, object, session);
	}

}
