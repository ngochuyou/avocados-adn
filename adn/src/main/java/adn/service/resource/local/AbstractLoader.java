/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.persister.entity.Loadable;
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

	protected void doLoad(Serializable id, LockOptions lockOptions, ResourceManager manager) {
		
	}
	
	protected List<Object> getResults(Serializable[] id, ResourceManager manager) {
		
		return null;
	}

	protected void preLoad(Serializable id, LockOptions lockOptions, ResourceManager manager,
			List<AfterLoadAction> afterLoadActions) {
		applyLock(id, lockOptions, manager, afterLoadActions);
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

}
