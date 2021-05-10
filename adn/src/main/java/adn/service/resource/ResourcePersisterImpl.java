package adn.service.resource;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.type.Type;

import adn.service.resource.factory.EntityManagerFactoryImplementor;
import adn.service.resource.metamodel.EntityPersisterImplementor;
import adn.service.resource.metamodel.type.AbstractSyntheticBasicType;
import adn.service.resource.metamodel.type.ExplicitlyHydratedType;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;

/**
 * @author Ngoc Huy
 *
 * @param <D>
 */
public class ResourcePersisterImpl<D> extends SingleTableEntityPersister
		implements ResourcePersister<D>, EntityPersisterImplementor<D>, ClassMetadata, SharedSessionUnwrapper {

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
	}

	@Override
	public EntityManagerFactoryImplementor getFactory() {
		return (EntityManagerFactoryImplementor) super.getFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourcePersister<D> getSubclassEntityPersister(Object instance, SessionFactoryImplementor factory) {
		if (!hasSubclasses()) {
			return null;
		}

		final String concreteEntityName = getEntityTuplizer().determineConcreteSubclassEntityName(instance, factory);

		if (concreteEntityName == null || getEntityName().equals(concreteEntityName)) {
			return this;
		}

		return (ResourcePersister<D>) getFactory().getEntityPersister(concreteEntityName);
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

}
