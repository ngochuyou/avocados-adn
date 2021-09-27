/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;

import adn.application.Common;
import adn.dao.generic.Result;
import adn.model.entities.PermanentEntity;
import adn.model.entities.metadata._FullyAuditedEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractPermanentEntityValidator<T extends PermanentEntity> extends AbstractEntityValidator<T> {

	private static final String EMPTY_ACTIVE_STATE = Common.notEmpty("Active state");

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (instance.isActive() == null) {
			result.bad().getMessages().put(_FullyAuditedEntity.active, EMPTY_ACTIVE_STATE);
		}

		return result;
	}

}
