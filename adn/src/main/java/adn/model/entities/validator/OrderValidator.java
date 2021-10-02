/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.dao.generic.Result;
import adn.model.Generic;
import adn.model.entities.Order;
import adn.model.entities.metadata._Order;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Order.class)
public class OrderValidator extends AbstractPermanentEntityValidator<Order> {

	private static final String INVALID_DELIVERY_FEE = Common.notNegative("Delivery fee");

	@Override
	public Result<Order> isSatisfiedBy(Session session, Serializable id, Order instance) {
		Result<Order> result = super.isSatisfiedBy(session, id, instance);

		BigDecimal deliveryFee = instance.getDeliveryFee();

		if (deliveryFee != null && deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put(_Order.deliveryFee, INVALID_DELIVERY_FEE);
		}

		return result;
	}

}
