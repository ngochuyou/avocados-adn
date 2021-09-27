/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.notEmpty;
import static adn.application.Common.notFuture;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
	private static final String INVALID_DROPPED_TIMESTAMP = notFuture("Dropped timestamp");
	private static final String INVALID_PRICE = normalizeString(
			String.format("%s and %s", notEmpty("Price"), Common.notNegative()));
	private static final String INVALID_APPROVED_TIMESTAMP = normalizeString(
			String.format("%s and %s %s", notEmpty("Approved timestamp"), notFuture(), Common.WHEN_APPROVED));
	private static final String UNNECESSARY_APPROVED_TIMESTAMP = String.format("%s %s",
			Common.mustEmpty("Approved timestamp"), Common.WHEN_UNAPPROVED);

	@Override
	public Result<ProductCost> isSatisfiedBy(Session session, Serializable id, ProductCost instance) {
		Result<ProductCost> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getProduct() == null) {
			result.bad().getMessages().put(_ProductCost.product, EMPTY_PRODUCT);
		}

		if (instance.getProvider() == null) {
			result.bad().getMessages().put(_ProductCost.provider, EMPTY_PROVIDER);
		}

		if (instance.getDroppedTimestamp() != null) {
			if (instance.getDroppedTimestamp().isAfter(LocalDateTime.now())) {
				result.bad().getMessages().put(_ProductCost.droppedTimestamp, INVALID_DROPPED_TIMESTAMP);
			}
		}

		if (instance.getCost() == null || instance.getCost().compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put(_ProductCost.cost, INVALID_PRICE);
		}

		boolean isApproved = instance.getApprovedBy() != null;
		boolean hasApprovedTimestamp = instance.getApprovedTimestamp() != null;

		if (isApproved) {
			if (!hasApprovedTimestamp || instance.getApprovedTimestamp().isAfter(LocalDateTime.now())) {
				result.bad().getMessages().put(_ProductCost.approvedTimestamp, INVALID_APPROVED_TIMESTAMP);
			}
		} else {
			if (hasApprovedTimestamp) {
				result.bad().getMessages().put(_ProductCost.approvedTimestamp, UNNECESSARY_APPROVED_TIMESTAMP);
			}
		}

		return result;
	}

}
