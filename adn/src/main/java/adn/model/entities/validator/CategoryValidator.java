/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.dao.generic.Result;
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
	// @formatter:off
	private static final String INVALID_DESCRIPTION = String.format(
			"Description can only contain alphabetic, numeric, %s characters and %s",
			Common.symbolNamesOf(
					'.', ',', '[', ']', '_', '-','+', '=',
					'/', '\\', '!', '@', '#', '$', '%', '^',
					'&', '*', '\'', '"', '?', '\s'),
			Common.hasLength(null, null, _Category.MAX_DESCRIPTION_LENGTH));
	// @formatter:on
	@Override
	public Result<Category> isSatisfiedBy(Session session, Serializable id, Category instance) {
		Result<Category> result = super.isSatisfiedBy(session, id, instance);

		if (StringHelper.hasLength(instance.getDescription())
				&& !_Category.DESCRIPTION_PATTERN.matcher(instance.getDescription()).matches()) {
			result.bad().getMessages().put(_Category.description, INVALID_DESCRIPTION);
		}

		return result;
	}

}
