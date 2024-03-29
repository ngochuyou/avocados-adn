/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;

import adn.application.Common;
import adn.application.Result;
import adn.helpers.HibernateHelper;
import adn.model.entities.Entity;
import adn.model.entities.metadata._Entity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractEntityValidator<T extends Entity> extends AbstractCompositeEntityValidator<T> {

	private static final String MISSING_ID = Common.notEmpty("Identifier");

	@Override
	public Result<T> isSatisfiedBy(Session session, T instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = new Result<>(instance);

		if (id == null && !HibernateHelper.isIdentifierAutoGenerated(instance.getClass())) {
			result.bad(_Entity.id, MISSING_ID);
		}

		return result;
	}

}
