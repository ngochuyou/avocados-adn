/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.application.Result;
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
	private static final String EMPTY_ITEMS = Common.notEmpty("Items information");
	private static final String INVALID_ADDRESS = String.format(
			"%s, can only contain alphabetic, numeric %s characters and %s", Common.notEmpty("Delivery address"),
			Common.symbolNamesOf('\s', '_', '-', '.', ',', '*', '\'', '"', '/', '&'),
			Common.hasLength(null, null, _Order.MAXIMUM_ADDRESS_LENGTH));
	private static final String EMPTY_DISTRICT = Common.notEmpty("Location information");
	private static final String INVALID_NOTE = String.format(
			"Your note can only contain alphabetic, numeric %s characters and %s",
			Common.symbolNamesOf('\s', '_', '-', '.', ',', '*', '\'', '"', '/', '&'),
			Common.hasLength(null, null, _Order.MAXIMUM_NOTE_LENGTH));

	@Override
	public Result<Order> isSatisfiedBy(Session session, Serializable id, Order instance) {
		Result<Order> result = super.isSatisfiedBy(session, id, instance);
		BigDecimal deliveryFee = instance.getDeliveryFee();

		if (deliveryFee != null && deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
			result.bad(_Order.deliveryFee, INVALID_DELIVERY_FEE);
		}

		if (instance.getItems().isEmpty()) {
			result.bad(_Order.items, EMPTY_ITEMS);
		}

		if (instance.getAddress() == null || !_Order.ADDRESS_PATTERN.matcher(instance.getAddress()).matches()) {
			result.bad(_Order.address, INVALID_ADDRESS);
		}

		if (instance.getDistrict() == null) {
			result.bad(_Order.district, EMPTY_DISTRICT);
		}

		if (instance.getNote() != null && !_Order.NOTE_PATTERN.matcher(instance.getNote()).matches()) {
			result.bad(_Order.note, INVALID_NOTE);
		}

		return result;
	}

}
