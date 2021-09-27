/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Category;
import adn.model.entities.metadata._Category;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Category.class)
public class CategoryValidator extends AbstractPermanentEntityValidator<Category> {

	@Override
	public Result<Category> isSatisfiedBy(Session session, Category instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<Category> isSatisfiedBy(Session session, Serializable id, Category instance) {
		Result<Category> result = super.isSatisfiedBy(session, id, instance);

		if (StringHelper.hasLength(instance.getDescription())
				&& instance.getDescription().length() > _Category.DESCRIPTION_LENGTH) {
			result.getMessages().put("description",
					String.format("Description can only contain %d chacracters", _Category.DESCRIPTION_LENGTH));
		}

		return result;
	}

}
