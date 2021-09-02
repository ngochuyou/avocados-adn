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
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductBuilder extends FactorBuilder<Product> {

	/**
	 * @param authService
	 */
	public ProductBuilder(AuthenticationService authService) {
		super(authService);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected <E extends Product> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setPrice(model.getPrice().setScale(4, RoundingMode.HALF_UP));
		target.setDescription(StringHelper.normalizeString(model.getDescription()));
		target.setImages(model.getImages().stream().filter(StringHelper::hasLength).collect(Collectors.toList()));
		target.setCategory(model.getCategory());

		return target;
	}

}
