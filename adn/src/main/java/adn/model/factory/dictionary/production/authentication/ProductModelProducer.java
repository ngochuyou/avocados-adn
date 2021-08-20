/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import static adn.helpers.Utils.ldt;

import java.util.Map;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Product> {

	@Override
	protected Map<String, Object> produceForAnonymous(Product entity, Map<String, Object> model) {
		Hibernate.initialize(entity.getCategory());
		
		model.put("id", entity.getId());
		model.put("price", entity.getPrice());
		model.put("category", entity.getCategory());
		model.put("images", entity.getImages());
		model.put("description", entity.getDescription());

		return model;
	}

	@Override
	protected Map<String, Object> produceForAdmin(Product entity, Map<String, Object> model) {
		return produceForAnonymous(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(Product entity, Map<String, Object> model) {
		model = produceForAnonymous(entity, model);

		model.put("createdTimestamp", ldt(entity.getCreatedTimestamp()));
		model.put("updatedTimestamp", ldt(entity.getUpdatedTimestamp()));

		return model;
	}

}
