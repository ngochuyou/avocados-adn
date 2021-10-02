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
import adn.model.entities.ProductCost;
import adn.model.entities.metadata._ProductCost;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductCost.class)
public class ProductCostValidator extends AbstractPermanentEntityValidator<ProductCost> {

	private static final String EMPTY_PRODUCT = notEmpty("Product information");
	private static final String EMPTY_PROVIDER = notEmpty("Provider information");
	private static final String INVALID_COST = normalizeString(
			String.format("%s and %s", notEmpty("Cost amount"), Common.notNegative()));

	@Override
	public Result<ProductCost> isSatisfiedBy(Session session, Serializable id, ProductCost instance) {
		Result<ProductCost> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getProduct() == null) {
			result.bad().getMessages().put(_ProductCost.product, EMPTY_PRODUCT);
		}

		if (instance.getProvider() == null) {
			result.bad().getMessages().put(_ProductCost.provider, EMPTY_PROVIDER);
		}

		if (instance.getCost() == null || instance.getCost().compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put(_ProductCost.cost, INVALID_COST);
		}

		return result;
	}

}
