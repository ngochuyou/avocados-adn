/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.ProductCost;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductCost.class)
public class ProductCostBuilder extends AbstractPermanentEntityBuilder<ProductCost> {

	@Override
	public <E extends ProductCost> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		if (model.getCost() != null) {
			model.setCost(model.getCost().setScale(4, RoundingMode.HALF_UP));
		}

		return model;
	}

}
