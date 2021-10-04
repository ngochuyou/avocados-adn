/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.Base32.crockfords;

import java.io.Serializable;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.model.Generic;
import adn.model.entities.Order;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Order.class)
public class OrderBuilder extends AbstractPermanentEntityBuilder<Order> {

	private static final Logger logger = LoggerFactory.getLogger(OrderBuilder.class);

	@Override
	protected <E extends Order> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setStatus(model.getStatus());

		if (model.getDeliveryFee() != null) {
			target.setDeliveryFee(model.getDeliveryFee().setScale(4, RoundingMode.HALF_UP));
		}

		return target;
	}

	@Override
	public <E extends Order> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setCreatedTimestamp(LocalDateTime.now());

		ContextProvider.getCurrentSession().persist(model);
		id = model.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(CODE_GENERATION_MESSAGE, id));
		}

		model.setCode(crockfords.format((BigInteger) id));

		return model;
	}

}
