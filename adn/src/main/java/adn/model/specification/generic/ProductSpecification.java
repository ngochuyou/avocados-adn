/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.EntityUtils;
import adn.model.Generic;
import adn.model.entities.Category;
import adn.model.entities.Product;
import adn.model.entities.generators.CategoryIdGenerator;
import adn.service.services.ProductService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductSpecification extends FactorSpecification<Product> {

	@Override
	public Result<Product> isSatisfiedBy(Session session, Product instance) {
		return isSatisfiedBy(session, EntityUtils.getIdentifier(instance), instance);
	}

	@Override
	public Result<Product> isSatisfiedBy(Session session, Serializable id, Product instance) {
		Result<Product> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put("price", "Price cannot be negative");
		}

		if (instance.getImages() != null && instance.getImages().size() > ProductService.MAXIMUM_IMAGES_AMOUNT) {
			result.bad().getMessages().put("price", "Unable to upload more than 20 images");
		}

		Category category = instance.getCategory();

		if (category != null) {
			if (!((CategoryIdGenerator) EntityUtils.getEntityPersister(Category.class).getIdentifierGenerator())
					.hasId(category.getId())) {
				result.bad().getMessages().put("category", String.format("Unknown category %s", category.getId()));
			}
		} else {
			result.bad().getMessages().put("category", "Missing category informations");
		}

		return result;
	}

}
