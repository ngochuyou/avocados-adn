/**
 * 
 */
package adn.model.entities.specification;

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
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.metadata._ProductProviderDetail;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductProviderDetail.class)
public class ProductProviderDetailSpecification extends PermanentEntitySpecification<ProductProviderDetail> {

	private static final String EMPTY_PRODUCT = notEmpty("Product information");
	private static final String EMPTY_PROVIDER = notEmpty("Provider information");
	private static final String INVALID_DROPPED_TIMESTAMP = notFuture("Dropped timestamp");
	private static final String INVALID_PRICE = normalizeString(
			String.format("%s and %s", notEmpty("Price"), Common.notNegative()));
	private static final String EMPTY_CREATOR = notEmpty("Creator information");
	private static final String INVALID_APPROVED_TIMESTAMP = normalizeString(
			String.format("%s and %s %s", notEmpty("Approved timestamp"), notFuture(), Common.WHEN_APPROVED));
	private static final String UNNECESSARY_APPROVED_TIMESTAMP = String.format("%s %s",
			Common.mustEmpty("Approved timestamp"), Common.WHEN_UNAPPROVED);

	@Override
	public Result<ProductProviderDetail> isSatisfiedBy(Session session, Serializable id,
			ProductProviderDetail instance) {
		Result<ProductProviderDetail> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getProduct() == null) {
			result.bad().getMessages().put(_ProductProviderDetail.product, EMPTY_PRODUCT);
		}

		if (instance.getProvider() == null) {
			result.bad().getMessages().put(_ProductProviderDetail.provider, EMPTY_PROVIDER);
		}

		if (instance.getDroppedTimestamp() != null || instance.getDroppedTimestamp().isAfter(LocalDateTime.now())) {
			result.bad().getMessages().put(_ProductProviderDetail.droppedTimestamp, INVALID_DROPPED_TIMESTAMP);
		}

		if (instance.getPrice() == null || instance.getPrice().compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put(_ProductProviderDetail.price, INVALID_PRICE);
		}

		if (instance.getCreatedBy() == null) {
			result.bad().getMessages().put(_ProductProviderDetail.createdBy, EMPTY_CREATOR);
		}

		boolean isApproved = instance.getApprovedBy() != null;
		boolean hasApprovedTimestamp = instance.getApprovedTimestamp() != null;

		if (isApproved) {
			if (!hasApprovedTimestamp || instance.getApprovedTimestamp().isAfter(LocalDateTime.now())) {
				result.bad().getMessages().put(_ProductProviderDetail.approvedTimestamp, INVALID_APPROVED_TIMESTAMP);
			}
		} else {
			if (hasApprovedTimestamp) {
				result.bad().getMessages().put(_ProductProviderDetail.approvedTimestamp,
						UNNECESSARY_APPROVED_TIMESTAMP);
			}
		}

		return result;
	}

}
