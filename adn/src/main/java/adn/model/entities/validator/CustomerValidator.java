/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.application.Result;
import adn.dao.generic.GenericRepository;
import adn.model.Generic;
import adn.model.entities.Customer;
import adn.model.entities.metadata._Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Customer.class)
public class CustomerValidator extends UserValidator<Customer> {

	private static final String NEGATIVE_POINT = Common.notNegative("Prestige point");
	private static final String MISSING_SUBSCRIPTION = Common.notEmpty("Subscription information");

	public CustomerValidator(GenericRepository genericRepository) {
		super(genericRepository);
	}

	@Override
	public Result<Customer> isSatisfiedBy(Session session, Serializable id, Customer instance) {
		Result<Customer> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getPrestigePoint() < 0) {
			result.bad(_Customer.prestigePoint, NEGATIVE_POINT);
		}

		if (instance.isSubscribed() == null) {
			result.bad(_Customer.subscribed, MISSING_SUBSCRIPTION);
		}

		return result;
	}

}
