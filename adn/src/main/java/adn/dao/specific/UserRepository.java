/**
 * 
 */
package adn.dao.specific;

import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import adn.application.context.ContextProvider;
import adn.dao.generic.GenericRepository.Selector;
import adn.helpers.HibernateHelper;
import adn.model.entities.Customer;
import adn.model.entities.metadata._Customer;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class UserRepository {

	private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

//	private final GenericRepositoryImpl genericRepository;
//
//	public UserRepository(GenericRepositoryImpl genericRepository) {
//		this.genericRepository = genericRepository;
//	}

	public List<Object[]> findCustomerCart(String customerId, Selector<Customer> selector) {
		Session session = ContextProvider.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		Root<Customer> root = criteriaQuery.from(Customer.class);

		criteriaQuery.multiselect(selector.select(root, criteriaQuery, builder))
				.where(builder.equal(root.get(_Customer.id), customerId));

		Query<Tuple> query = session.createQuery(criteriaQuery);

		if (logger.isDebugEnabled()) {
			logger.debug(query.getQueryString());
		}

		return HibernateHelper.toRows(query.list());
	}

}
