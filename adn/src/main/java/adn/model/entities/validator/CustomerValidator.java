/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.model.Generic;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Customer.class)
public class CustomerValidator extends AccountValidator<Customer> {

	@Override
	public Result<Customer> isSatisfiedBy(Session session, Customer instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<Customer> isSatisfiedBy(Session session, Serializable id, Customer instance) {
		Result<Customer> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getPrestigePoint() < 0) {
			result.bad().getMessages().put("prestigePoint", "Prestige point can not be negative");
		}

		return result;
	}

}
