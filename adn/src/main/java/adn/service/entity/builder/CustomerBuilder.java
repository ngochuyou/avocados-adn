/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Customer.class)
public class CustomerBuilder extends UserBuilder<Customer> {

	@Override
	protected <E extends Customer> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setPrestigePoint(model.getPrestigePoint());
		target.setSubscribed(Optional.ofNullable(model.isSubscribed()).orElse(Boolean.FALSE));

		return target;
	}

	@Override
	public <E extends Customer> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setLocked(Boolean.TRUE);

		return model;
	}

}
