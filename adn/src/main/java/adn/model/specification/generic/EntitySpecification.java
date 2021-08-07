/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;

import org.hibernate.Session;

import adn.dao.generic.Result;
import adn.helpers.EntityUtils;
import adn.model.entities.Entity;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
public abstract class EntitySpecification<T extends Entity> implements Specification<T> {

	@Override
	public Result<T> isSatisfiedBy(Session session, T instance) {
		return isSatisfiedBy(session, EntityUtils.getIdentifier(instance), instance);
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = new Result<>(instance);

		if (id == null && !EntityUtils.isIdentifierAutoGenerated(instance.getClass())) {
			result.bad().getMessages().put("id", "Id can not be empty");
		}

		return result;
	}

}
