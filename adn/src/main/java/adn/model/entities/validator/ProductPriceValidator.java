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
import adn.dao.generic.Result;
import adn.model.Generic;
import adn.model.entities.ProductPrice;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductPrice.class)
public class ProductPriceValidator extends AbstractPermanentEntityValidator<ProductPrice> {

	private static final String INVALID_PRICE = normalizeString(
			String.format("%s and %s", notEmpty("Price amount"), Common.notNegative()));

	@Override
	public Result<ProductPrice> isSatisfiedBy(Session session, Serializable id, ProductPrice instance) {
		Result<ProductPrice> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getPrice() == null || instance.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put(_ProductPrice.price, INVALID_PRICE);
		}

		return result;
	}

}
