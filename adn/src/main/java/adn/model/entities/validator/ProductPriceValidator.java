/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.notEmpty;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.application.Result;
import adn.model.Generic;
import adn.model.entities.ProductPrice;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductPrice.class)
public class ProductPriceValidator extends AbstractPermanentEntityValidator<ProductPrice> {

	private static final String MISSING_PRODUCT = notEmpty("Product information");
	private static final String INVALID_PRICE = normalizeString(
			String.format("%s and %s", notEmpty("Price amount"), Common.notNegative()));

	@Override
	public Result<ProductPrice> isSatisfiedBy(Session session, Serializable id, ProductPrice instance) {
		Result<ProductPrice> result = super.isSatisfiedBy(session, id, instance);
		ProductPriceId identifier = (ProductPriceId) id;

		if (identifier.getProductId() == null) {
			result.bad(_ProductPrice.product, MISSING_PRODUCT);
		}

		if (instance.getPrice() == null || instance.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			result.bad(_ProductPrice.price, INVALID_PRICE);
		}

		return result;
	}

}
