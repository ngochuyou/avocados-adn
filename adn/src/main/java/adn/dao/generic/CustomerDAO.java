/**
 * 
 */
package adn.dao.generic;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import adn.helpers.StringHelper;
import adn.model.Genetized;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Customer.class)
public class CustomerDAO extends AccountDAO<Customer> {

	@Override
	@Transactional
	public Customer defaultBuild(Customer model) {
		// TODO Auto-generated method stub
		model = super.defaultBuild(model);
		model.setAddress(StringHelper.normalizeString(model.getAddress()));

		return model;
	}

	@Override
	public Customer updateBuild(Customer model) {
		// TODO Auto-generated method stub
		model = super.updateBuild(model);

		Customer persisted = sessionFactory.getCurrentSession().get(Customer.class, model.getId());

		persisted.setAddress(model.getAddress());
		persisted.setPrestigePoint(model.getPrestigePoint());

		return model;
	}

}
