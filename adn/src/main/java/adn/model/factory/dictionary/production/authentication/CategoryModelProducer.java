/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Category;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Category.class)
public class CategoryModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Category> {

	@Override
	protected Map<String, Object> produceForPersonnel(Category category, Map<String, Object> model) {
		model.put("id", category.getId());
		model.put("description", category.getDescription());

		return model;
	}

}
