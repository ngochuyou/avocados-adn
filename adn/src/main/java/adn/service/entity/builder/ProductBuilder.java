/**
 * 
 */
package adn.service.entity.builder;

import java.math.RoundingMode;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductBuilder extends FactorBuilder<Product> {

	@Override
	protected <E extends Product> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setPrice(model.getPrice().setScale(4, RoundingMode.HALF_UP));
		target.setDescription(StringHelper.normalizeString(model.getDescription()));
		target.setImages(model.getImages().stream().filter(StringHelper::hasLength).collect(Collectors.toSet()));
		target.setCategory(model.getCategory());

		return target;
	}

}
