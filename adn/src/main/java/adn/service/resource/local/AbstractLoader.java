/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
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
			LockOptions lockOptions) {
		logger.debug(String.format("Loading resource %s", id));

		List<?> result = null;

		manager.getPersistenceContext().beforeLoad();

		try {
			result = getResults(id, manager, lockOptions, null);
		} finally {
			manager.getPersistenceContext().afterLoad();
		}

		logger.debug(String.format("Done loading", id));

		return result;
	}

	private List<?> getResults(Serializable id, ResourceManager manager, LockOptions lockOptions,
			RowSelection selection) {
		List<?> result;
		int maxRows = selection != null ? selection.getMaxRows() : Integer.MAX_VALUE; // best not be INTEGER.MAX_VALUE
		List<AfterLoadAction> afterLoadActions = new ArrayList<>();

		try {
			applyLock(id, lockOptions, manager, afterLoadActions);
			result = manager.getResourceManagerFactory().getStorage().select(id);
			processResults(result, manager, maxRows, lockOptions.getLockMode());

			return result;
		} finally {
			// cleanups, informs if needed
		}
	}

	private void processResults(List<?> results, ResourceManager resourceManager, int maxRow, LockMode lockMode) {
		ResourceContext context = resourceManager.getPersistenceContext();
		Loadable persister = getPersister().unwrap(Loadable.class);
		Object instance;
		ResourceKey<?>[] keys = getResourceKeys(results);

		for (int i = 0; i < maxRow && i < results.size(); i++) {
			instance = results.get(i);

			if (context.contains(keys[i])) {
				doWhenInContext(persister, instance, keys[i], lockMode, resourceManager);
				continue;
			}

			doWhenNotInContext(persister, instance, keys[i], lockMode, resourceManager);
		}
	}

	private void doWhenNotInContext(Loadable persister, Object instance, ResourceKey<?> key, LockMode requestedLockMode,
			ResourceManager manager) {

	}

	private void doWhenInContext(Loadable persister, Object instance, ResourceKey<?> key, LockMode requestedLockMode,
			ResourceManager manager) {
		if (!persister.isInstance(instance)) {
			throw new IllegalStateException(
					String.format("Type mismatch between loaded instance and persister %s vs %s", instance.getClass(),
							persister.getMappedClass()));
		}

		if (requestedLockMode == LockMode.NONE) {
			// exit if locked with NONE
			return;
		}

		ResourceEntry<?> entry = manager.getPersistenceContext().getEntry(instance);

		if (entry.getLockMode().lessThan(requestedLockMode)) {
			// upgrade lock mode
			if (persister.isVersioned()) {
				versionCheck(entry, instance, manager);
			}

			entry.setLockMode(requestedLockMode);
		}
	}

	private void versionCheck(ResourceEntry<?> entry, Object instance, ResourceManager manager) {
		Object managedVersion = entry.getVersion();

		logger.debug("Checking version of resource " + entry.getResourceKey().getIdentifier());

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

	private ResourceKey<?>[] getResourceKeys(List<?> instances) {
		return instances.stream().map(this::produceResourceKey).toArray(ResourceKey<?>[]::new);
	}

	private ResourceKey<?> produceResourceKey(Object o) {
		ResourcePersister<?> persister;

		return new ResourceKey<>((persister = getPersister()).getIdentifier(o, null), persister);
	}

	protected void applyLock(Serializable id, LockOptions lockOptions, ResourceManager manager,
			List<AfterLoadAction> afterLoadActions) {
		if (lockOptions == null || lockOptions.getLockMode() == LockMode.NONE
				|| lockOptions.getLockMode() == LockMode.UPGRADE_SKIPLOCKED) {
			// no action
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

}
