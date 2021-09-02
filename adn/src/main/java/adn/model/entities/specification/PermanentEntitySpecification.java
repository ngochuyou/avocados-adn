/**
 * 
 */
package adn.model.entities.specification;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.model.Generic;
import adn.model.entities.PermanentEntity;
import adn.model.entities.metadata._Factor;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = PermanentEntity.class)
public class PermanentEntitySpecification<T extends PermanentEntity> extends EntitySpecification<T> {

	private static final String EMPTY_ACTIVE_STATE = "Active state must not be empty";

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (instance.isActive() == null) {
			result.bad().getMessages().put(_Factor.active, EMPTY_ACTIVE_STATE);
		}

		return result;
	}

}
