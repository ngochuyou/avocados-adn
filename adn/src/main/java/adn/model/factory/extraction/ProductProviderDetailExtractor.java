/**
 * 
 */
package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.id.ProductProviderDetailId;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductProviderDetail.class, modelGene = ProductProviderDetail.class)
public class ProductProviderDetailExtractor
		implements PojoEntityExtractor<ProductProviderDetail, ProductProviderDetail> {

	@Override
	public ProductProviderDetail extract(ProductProviderDetail model) {
		ProductProviderDetailId embeddedId = new ProductProviderDetailId();

		embeddedId.setProductId(model.getProduct().getId());
		embeddedId.setProviderId(model.getProvider().getId());

		model.setId(embeddedId);

		return model;
	}

	@Override
	public ProductProviderDetail extract(ProductProviderDetail source, ProductProviderDetail target) {
		ProductProviderDetailId embeddedId = new ProductProviderDetailId();

		embeddedId.setProductId(source.getProduct().getId());
		embeddedId.setProviderId(source.getProvider().getId());

		target.setId(embeddedId);

		return target;
	}

}
