/**
 * 
 */
package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.ProductCost;
import adn.model.entities.id.ProductCostId;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductCost.class, modelGene = ProductCost.class)
public class ProductProviderDetailExtractor
		implements PojoEntityExtractor<ProductCost, ProductCost> {

	@Override
	public ProductCost extract(ProductCost model) {
		ProductCostId embeddedId = new ProductCostId();

		embeddedId.setProductId(model.getProduct().getId());
		embeddedId.setProviderId(model.getProvider().getId());

		model.setId(embeddedId);

		return model;
	}

	@Override
	public ProductCost extract(ProductCost source, ProductCost target) {
		ProductCostId embeddedId = new ProductCostId();

		embeddedId.setProductId(source.getProduct().getId());
		embeddedId.setProviderId(source.getProvider().getId());

		target.setId(embeddedId);

		return target;
	}

}
