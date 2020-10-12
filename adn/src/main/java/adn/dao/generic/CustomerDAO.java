/**
 * 
 */
package adn.dao.generic;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import adn.model.Genetized;
import adn.model.entities.Customer;
import adn.utilities.Strings;

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
		model.setAddress(Strings.normalizeString(model.getAddress()));

		return model;
	}

	@Override
	public Customer updateBuild(Customer model) {
		// TODO Auto-generated method stub
		Customer persisted = super.updateBuild(model);

		persisted.setAddress(model.getAddress());
		persisted.setPrestigePoint(model.getPrestigePoint());

		return persisted;
	}

}
