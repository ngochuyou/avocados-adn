/**
 * 
 */
package adn.dao.specific;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.application.context.ContextProvider;
import adn.dao.generic.GenericRepository.Selector;
import adn.dao.generic.GenericRepositoryImpl;
import adn.helpers.HibernateHelper;
import adn.model.entities.Customer;
import adn.model.entities.Product;
import adn.model.entities.ProductPrice;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class UserRepository {

	private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

	private final GenericRepositoryImpl genericRepository;

	public UserRepository(GenericRepositoryImpl genericRepository) {
		this.genericRepository = genericRepository;
	}

	public List<Object[]> findCustomerCart(String customerId, Selector<Customer> selector) {
		return findCustomerCart(customerId, selector, LockModeType.NONE);
	}

	public List<Object[]> findCustomerCart(String customerId, Selector<Customer> selector, LockModeType lockMode) {
		return findCustomerCart(customerId, selector, (root, query, builder) -> builder.conjunction(), lockMode);
	}

	public List<Object[]> findCustomerCart(String customerId, Selector<Customer> selector, Specification<Customer> spec,
			LockModeType lockMode) {
		Session session = ContextProvider.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		Root<Customer> root = criteriaQuery.from(Customer.class);

		criteriaQuery.multiselect(selector.select(root, criteriaQuery, builder)).where(
				genericRepository.resolvePredicate(Customer.class, root, criteriaQuery, builder, spec),
				builder.equal(root.get(_Customer.id), customerId));

		Query<Tuple> query = session.createQuery(criteriaQuery);

		query.setLockMode(lockMode);

		if (logger.isDebugEnabled()) {
			logger.debug(query.getQueryString());
		}

		return HibernateHelper.toRows(query.list());
	}

	public List<Object[]> findCustomerCartForPlacement(String customerId) {
		Session session = ContextProvider.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
		Root<Customer> root = criteriaQuery.from(Customer.class);
		Join<Object, Object> cartJoin = root.join(_Customer.cart);
		Join<Product, ProductPrice> priceJoin = cartJoin.join(_Item.product).join(_Product.prices);
		List<Selection<?>> selections = new ArrayList<>();

		selections.add(priceJoin.get(_ProductPrice.price));

		Path<Object> productPriceIdPath = priceJoin.get(_ProductPrice.id);

		criteriaQuery
				.multiselect(Arrays.asList(cartJoin.get(_Item.id), cartJoin.get(_Item.status),
						priceJoin.get(_ProductPrice.price)))
				.where(genericRepository.resolvePredicate(Customer.class, root, criteriaQuery, builder, null),
						builder.equal(root.get(_Customer.id), customerId),
						builder.and(
								builder.isNotNull(priceJoin.get(_ProductPrice.approvalInformations)
										.get(_ProductPrice.approvedTimestamp)),
								builder.between(builder.literal(LocalDateTime.now()),
										productPriceIdPath.get(_ProductPrice.appliedTimestamp),
										productPriceIdPath.get(_ProductPrice.droppedTimestamp))));

		Query<Tuple> query = session.createQuery(criteriaQuery);

		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

		if (logger.isDebugEnabled()) {
			logger.debug(query.getQueryString());
		}

		return HibernateHelper.toRows(query.list());
	}

}
