/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.engine.internal.TwoPhaseLoad;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.type.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractLoader implements UniqueEntityLoader, SharedSessionUnwrapper {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	abstract Object load(Serializable id, Object optionalObject, ResourceManager manager);

	abstract Object load(Serializable id, Object optionalObject, ResourceManager manager, LockOptions lockOptions);

	@Override
	public Object load(Serializable id, Object optionalObject, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		return load(id, optionalObject, unwrapSession(session));
	}

	@Override
	public Object load(Serializable id, Object optionalObject, SharedSessionContractImplementor session,
			LockOptions lockOptions) {
		// TODO Auto-generated method stub
		return load(id, optionalObject, unwrapSession(session), lockOptions);
	}

	protected List<?> doLoad(Serializable id, ResourceManager manager, ResourcePersister<?> persister,
			LockOptions lockOptions) throws HibernateException, SQLException {
		logger.debug(String.format("Loading resource %s", id));

		List<?> result = null;

		manager.getPersistenceContext().beforeLoad();

		try {
			result = getResults(new Serializable[] { id }, persister, manager, lockOptions, null);
		} finally {
			manager.getPersistenceContext().afterLoad();
		}

		logger.debug(String.format("Done loading", id));

		return result;
	}

	private List<Object> getResults(Serializable[] ids, ResourcePersister<?> persister, ResourceManager manager,
			LockOptions lockOptions, RowSelection selection) throws HibernateException, SQLException {
		List<Object> result;
		int maxRows = selection != null ? selection.getMaxRows() : Integer.MAX_VALUE; // best not be INTEGER.MAX_VALUE
		List<AfterLoadAction> afterLoadActions = new ArrayList<>();

		try {
			applyLock(ids, lockOptions, manager, afterLoadActions);
			result = manager.getResourceManagerFactory().getStorage().select(ids);
			processResults(result, manager, maxRows, lockOptions.getLockMode());

			return result;
		} finally {
			// cleanups, informs if needed
		}
	}

	private void processResults(List<Object> resultSet, ResourceManager resourceManager, int maxRow, LockMode lockMode)
			throws HibernateException, SQLException {
		PersistenceContext context = resourceManager.getPersistenceContext();
		ResourcePersister<?> persister = getPersister();
		EntityKey[] keys = generateKeys(resultSet);
		List<Object> results = new ArrayList<>();

		for (int i = 0; i < maxRow && i < resultSet.size(); i++) {
			if (context.containsEntity(keys[i])) {
				results.add(doWhenInContext(persister.unwrap(Loadable.class), resultSet.get(i), keys[i], lockMode,
						resourceManager));
				continue;
			}

			results.add(doWhenNotInContext(persister, resultSet.get(i), keys[i], lockMode, resourceManager));
		}
	}

	private Object doWhenNotInContext(ResourcePersister<?> persister, Object object, EntityKey key,
			LockMode requestedLockMode, ResourceManager manager) throws HibernateException, SQLException {
		Object instanceObject = persister.instantiate(key.getIdentifier(), manager);

		return hydrateEntity(key, object, instanceObject, persister.unwrap(Loadable.class), manager,
				requestedLockMode == LockMode.NONE ? LockMode.READ : requestedLockMode);
	}

	private Object hydrateEntity(EntityKey key, Object row, Object instance, Loadable persister,
			ResourceManager manager, LockMode requiredLockMode) throws HibernateException, SQLException {
		Serializable id = key.getIdentifier();
		Object[] hydratedValues = hydrateRowValues(key, row, persister, manager, requiredLockMode);

		logger.debug("%s#%s Hydrated values [%s]", persister.getEntityName(), key.getIdentifier(),
				Stream.of(hydratedValues.toString()).collect(Collectors.joining(", ")));
		// add entry
		TwoPhaseLoad.addUninitializedEntity(key, instance, persister, requiredLockMode, manager);
		// actually hydrate the entity
		((ResourcePersister<?>) persister).hydrate(hydratedValues, instance);
		// update entry
		TwoPhaseLoad.postHydrate(persister, id, hydratedValues, null, instance, requiredLockMode, manager);

		return instance;
	}

	private Object[] hydrateRowValues(EntityKey key, Object row, Loadable persister, ResourceManager manager,
			LockMode requestedLockMode) throws HibernateException, SQLException {
		Serializable id = key.getIdentifier();

		return persister.hydrate(null, id, row, manager.getResourceManagerFactory().getMetamodel()
				.entityPersister(persister.getRootEntityName()).unwrap(Loadable.class), null, true, manager);
	}

	private Object doWhenInContext(Loadable persister, Object instance, EntityKey key, LockMode requestedLockMode,
			ResourceManager manager) {
		if (!persister.isInstance(instance)) {
			throw new IllegalStateException(
					String.format("Type mismatch between loaded instance and persister %s vs %s", instance.getClass(),
							persister.getMappedClass()));
		}

		if (requestedLockMode == LockMode.NONE) {
			// exit if locked with NONE
			return manager.getPersistenceContext().getEntity(key);
		}

		ResourceEntry<?> entry = (ResourceEntry<?>) manager.getPersistenceContext().getEntry(instance);

		if (entry.getLockMode().lessThan(requestedLockMode)) {
			// upgrade lock mode
			if (persister.isVersioned()) {
				versionCheck(entry, instance, manager);
			}

			entry.setLockMode(requestedLockMode);
		}

		return manager.getPersistenceContext().getEntity(key);
	}

	private void versionCheck(ResourceEntry<?> entry, Object instance, ResourceManager manager) {
		Object managedVersion = entry.getVersion();

		logger.debug("Checking version of resource " + entry.getEntityKey().getIdentifier());

		if (managedVersion != null) {
			Object loadedVersion = entry.getPersister().getVersion(instance);
			VersionType<?> type = entry.getPersister().getVersionType();

			if (!type.isEqual(loadedVersion, type)) {
				throw new IllegalStateException(
						String.format("Optimistic lock check failed, loaded version is %s, while managed version is %s",
								loadedVersion, managedVersion));
			}
		}
	}

	private EntityKey[] generateKeys(List<Object> instances) throws HibernateException, SQLException {
		EntityKey[] keys = new EntityKey[instances.size()];
		int i = 0;

		for (Object o : instances) {
			keys[i++] = produceResourceKey(o);
		}

		return keys;
	}

	private EntityKey produceResourceKey(Object o) throws HibernateException, SQLException {
		ResourcePersister<?> persister;

		return new EntityKey(
				(Serializable) (persister = getPersister()).getIdentifierType().hydrate(null, null, null, o),
				persister);
	}

	protected void applyLock(Serializable id, LockOptions lockOptions, ResourceManager manager,
			List<AfterLoadAction> afterLoadActions) {
		if (lockOptions == null || lockOptions.getLockMode() == LockMode.NONE
				|| lockOptions.getLockMode() == LockMode.UPGRADE_SKIPLOCKED) {
			logger.debug(String.format("Skipping %s on %s", LockMode.NONE, id.toString()));

			return;
		}

		logger.debug("Applying " + lockOptions.getLockMode() + " on " + id);

		afterLoadActions.add(new AfterLoadAction() {
			@Override
			public void afterLoad(SharedSessionContractImplementor session, Object entity, Loadable persister) {
				// TODO Auto-generated method stub
				unwrapSession(session).unwrapManager(SessionImplementor.class).buildLockRequest(lockOptions)
						.lock(persister.getEntityName(), entity);
			}
		});
	}

	abstract public ResourcePersister<?> getPersister();

	abstract public String[] getIdentifierValueNames();

}
