/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.dao.DatabaseInteractionResult;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Category;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Category.class)
public class CategorySpecification extends FactorSpecification<Category> {

	@Override
	public DatabaseInteractionResult<Category> isSatisfiedBy(Serializable id, Category instance) {
		DatabaseInteractionResult<Category> result = super.isSatisfiedBy(id, instance);

		if (StringHelper.hasLength(instance.getDescription()) && instance.getDescription().length() > Category.DESCRIPTION_LENGTH) {
			result.getMessages().put("description",
					String.format("Description can only contain %d chacracters", Category.DESCRIPTION_LENGTH));
		}

		return result;
	}

}
