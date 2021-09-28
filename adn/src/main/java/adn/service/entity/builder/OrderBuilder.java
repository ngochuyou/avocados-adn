/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Order;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Order.class)
public class OrderBuilder extends AbstractPermanentEntityBuilder<Order> {

	@Override
	protected <E extends Order> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setStatus(model.getStatus());
		target.setDeliveryFee(model.getDeliveryFee().setScale(4, RoundingMode.HALF_UP));

		return target;
	}

	@Override
	public <E extends Order> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setCreatedTimestamp(LocalDateTime.now());

		return model;
	}

}
