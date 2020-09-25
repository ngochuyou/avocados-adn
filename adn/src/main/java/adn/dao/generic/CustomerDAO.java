/**
 * 
 */
package adn.dao.generic;

import org.springframework.stereotype.Repository;

import adn.dao.BaseDAO;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.entities.Customer;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Customer.class)
public class CustomerDAO extends BaseDAO implements GenericDAO<Customer> {

	@Override
	public Customer defaultBuild(Customer model) {
		// TODO Auto-generated method stub
		model.setAddress(Strings.normalizeString(model.getAddress()));

		return model;
	}

}
