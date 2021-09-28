/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductPrice.class)
public class ProductPriceBuilder extends AbstractPermanentEntityBuilder<ProductPrice> {

	@Override
	protected <E extends ProductPrice> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);
		
		target.setPrice(target.getPrice().setScale(4, RoundingMode.HALF_UP));
		
		return target;
	}
	
	@Override
	public <E extends ProductPrice> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);
		
		model.setDroppedTimestamp(null);
		
		return model;
	}
	
}
