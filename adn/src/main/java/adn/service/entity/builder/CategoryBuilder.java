/**
 * 
 */
package adn.service.entity.builder;

import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Category;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Category.class)
public class CategoryBuilder extends AbstractPermanentEntityBuilder<Category> {

	@Override
	protected <E extends Category> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);
		target.setDescription(StringHelper.normalizeString(model.getDescription()));

		return target;
	}

}
