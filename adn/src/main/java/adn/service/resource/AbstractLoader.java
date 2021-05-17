/**
 * 
 */
package adn.service.resource;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.engine.internal.TwoPhaseLoad;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.loader.Loader;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.persister.entity.Loadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.engine.ResultSetImplementor;
import adn.service.resource.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractLoader extends Loader implements UniqueEntityLoader, SharedSessionUnwrapper {

	/**
	 * @param factory
	 */
	public AbstractLoader(SessionFactoryImplementor factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

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

	protected List<Object> doLoad(Serializable id, ResourceManager manager, ResourcePersister<?> persister,
			LockOptions lockOptions) throws HibernateException, SQLException {
		List<Object> result = null;

		manager.getPersistenceContext().beforeLoad();

		try {
			result = getResults(new Serializable[] { id }, persister, manager, lockOptions, null);
		} finally {
			manager.getPersistenceContext().afterLoad();
		}

		return result;
	}

	private List<Object> getResults(Serializable[] ids, ResourcePersister<?> persister, ResourceManager manager,
			LockOptions lockOptions, RowSelection selection) throws HibernateException, SQLException {
		ResultSetImplementor result;
		int maxRows = selection != null ? selection.getMaxRows() : Integer.MAX_VALUE; // best not be INTEGER.MAX_VALUE
		List<AfterLoadAction> afterLoadActions = new ArrayList<>();

		try {
			applyLock(ids, lockOptions, manager, afterLoadActions);
			result = ((EntityManagerFactoryImplementor) manager.getFactory()).getStorage().select(ids);

			return processResults(result, persister, manager, maxRows, lockOptions.getLockMode(), afterLoadActions);
		} finally {
			// cleanups, informs if needed
		}
	}

	private List<Object> processResults(ResultSetImplementor resultSet, ResourcePersister<?> persister,
			ResourceManager resourceManager, int maxRow, LockMode lockMode, List<AfterLoadAction> afterLoadActions)
			throws HibernateException, SQLException {
		EntityKey[] keys = generateKeys(resultSet, resourceManager);
		List<Object> results = getRowsFromResultSet(keys, resultSet, maxRow, lockMode, persister, resourceManager);

		resolveResults(results, resultSet, resourceManager.getPersistenceContext().isDefaultReadOnly(),
				afterLoadActions, resourceManager);

		return results;
	}

	@SuppressWarnings({ "resource", "deprecation" })
	private void resolveResults(List<Object> hydratedObjects, ResultSet rs, boolean readOnly,
			List<AfterLoadAction> afterLoadActions, ResourceManager resourceManager) {
		PreLoadEvent ple = new PreLoadEvent((EventSource) resourceManager);

		for (Object hydratedObject : hydratedObjects) {
			TwoPhaseLoad.initializeEntity(hydratedObject, readOnly, resourceManager, ple,
					resourceManager.getFactory().getFastSessionServices().eventListenerGroup_PRE_LOAD.listeners());
		}

		for (Object hydratedObject : hydratedObjects) {
			TwoPhaseLoad.afterInitialize(hydratedObject, resourceManager);
		}

		PostLoadEvent postLE = new PostLoadEvent((EventSource) resourceManager);

		for (Object hydratedObject : hydratedObjects) {
			TwoPhaseLoad.postLoad(hydratedObject, resourceManager, postLE);

			for (AfterLoadAction afterLoadAction : afterLoadActions) {
				final EntityEntry entityEntry = resourceManager.getPersistenceContext().getEntry(hydratedObject);

				if (entityEntry == null) {
					throw new HibernateException("Could not locate EntityEntry immediately after two-phase load");
				}

				afterLoadAction.afterLoad(resourceManager, hydratedObject, (Loadable) entityEntry.getPersister());
			}
		}
	}

	private List<Object> getRowsFromResultSet(EntityKey[] keys, ResultSet resultSet, int maxRow,
			LockMode requestedLockMode, ResourcePersister<?> persister, ResourceManager resourceManager)
			throws HibernateException, SQLException {
		List<Object> results = new ArrayList<>();
		PersistenceContext context = resourceManager.getPersistenceContext();
		Object object;

		for (int i = 0; i < maxRow && i < resultSet.getFetchSize(); i++, resultSet.next()) {
			if ((object = context.getEntity(keys[i])) != null) {
				results.add(doWhenInContext(keys[i], object, requestedLockMode, resultSet, i + 1, persister,
						resourceManager));
				continue;
			}

			object = persister.instantiate(keys[i].getIdentifier(), resourceManager);
			results.add(doWhenNotInContext(keys[i], object, requestedLockMode, resultSet, i + 1, persister,
					resourceManager));
		}

		return results;
	}

	private Object doWhenNotInContext(EntityKey key, Object object, LockMode requestedLockMode, ResultSet rs, int row,
			ResourcePersister<?> persister, ResourceManager resourceManager) throws HibernateException, SQLException {
		hydrateEntity(key, object, requestedLockMode == LockMode.NONE ? LockMode.READ : requestedLockMode, rs, row,
				persister.unwrap(Loadable.class), resourceManager);

		return object;
	}

	private void hydrateEntity(EntityKey key, Object object, LockMode requestedLockMode, ResultSet rs, int row,
			Loadable persister, ResourceManager manager) throws HibernateException, SQLException {
		Serializable id = key.getIdentifier();
		// add entry
		TwoPhaseLoad.addUninitializedEntity(key, object, persister, requestedLockMode, manager);
		// actually hydrate the entity
		Object[] hydratedValues = hydrateRowValues(id, object, rs, row, persister, manager);
		// update entry
		TwoPhaseLoad.postHydrate(persister, id, hydratedValues, rs.getRowId(0), object, requestedLockMode, manager);
	}

	private Object[] hydrateRowValues(Serializable id, Object object, ResultSet rs, int row, Loadable persister,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		rs.absolute(row);

		return persister.hydrate(rs, id, object, persister, null, true, session);
	}

	private Object doWhenInContext(EntityKey key, Object existingInstance, LockMode requestedLockMode, ResultSet rs,
			int i, ResourcePersister<?> persister, ResourceManager resourceManager)
			throws HibernateException, SQLException {
		if (!persister.isInstance(existingInstance)) {
			throw new IllegalStateException(
					String.format("Type mismatch between loaded instance and persister [%s><%s]",
							existingInstance.getClass(), persister.getMappedClass()));
		}
		// exit if locked with NONE
		if (requestedLockMode == LockMode.NONE) {
			return resourceManager.getPersistenceContext().getEntity(key);
		}

		EntityEntry entry = resourceManager.getPersistenceContext().getEntry(existingInstance);

		if (entry.getLockMode().lessThan(requestedLockMode)) {
			// upgrade lock mode
			if (persister.isVersioned()) {
				versionCheck(key.getIdentifier(), existingInstance, rs, i, persister.unwrap(Loadable.class),
						resourceManager);
			}

			entry.setLockMode(requestedLockMode);
		}

		return resourceManager.getPersistenceContext().getEntity(key);
	}

	private void versionCheck(Serializable id, Object existingInstance, ResultSet rs, int i, Loadable persister,
			ResourceManager resourceManager) throws HibernateException, SQLException {
		Object managedVersion = resourceManager.getPersistenceContext().getEntry(existingInstance).getVersion();

		logger.debug("Checking version of resource " + id);

		rs.absolute(i);

		if (managedVersion != null) {
			Object loadedVersion = persister.getVersionType().hydrate(rs,
					persister.getPropertyColumnNames(persister.getVersionProperty()),
					resourceManager.unwrapManager(SharedSessionContractImplementor.class), persister);

			if (!persister.getVersionType().isEqual(loadedVersion, managedVersion)) {
				throw new IllegalStateException(String.format(
						"Optimistic lock check failed, loaded version is [%s], while managed version is [%s]",
						loadedVersion, managedVersion));
			}
		}
	}

	private EntityKey[] generateKeys(ResultSetImplementor resultSet, ResourceManager session)
			throws HibernateException, SQLException {
		EntityKey[] keys = new EntityKey[resultSet.getFetchSize()];

		resultSet.first();

		for (int i = 0; i < keys.length; i++, resultSet.next()) {
			keys[i++] = produceResourceKey(resultSet, session);
		}

		return keys;
	}

	private EntityKey produceResourceKey(ResultSetImplementor resultSet, ResourceManager session)
			throws HibernateException, SQLException {
		ResourcePersister<?> persister = getPersister();
		Serializable id = (Serializable) persister.getIdentifierType().hydrate(resultSet,
				new String[] { getPersister().getIdentifierPropertyName() }, session, persister);

		return session.generateEntityKey(id, persister);
	}

	protected void applyLock(Serializable id, LockOptions lockOptions, ResourceManager manager,
			List<AfterLoadAction> afterLoadActions) {
		if (lockOptions == null || lockOptions.getLockMode() == LockMode.NONE
				|| lockOptions.getLockMode() == LockMode.UPGRADE_SKIPLOCKED) {
			logger.trace(String.format("Skipping [%s] on [%s]", LockMode.NONE, id.toString()));

			return;
		}

		logger.debug(String.format("Applying [%s] on [%s]", lockOptions.getLockMode(), id.toString()));

		afterLoadActions.add(new AfterLoadAction() {
			@Override
			public void afterLoad(SharedSessionContractImplementor session, Object entity, Loadable persister) {
				logger.trace(String.format("Performing version check on [%s#%s]", entity.getClass().getName(),
						id.toString()));
				unwrapSession(session).unwrapManager(SessionImplementor.class).buildLockRequest(lockOptions)
						.lock(persister.getEntityName(), entity);
			}
		});
	}

	abstract public ResourcePersister<?> getPersister();

}
