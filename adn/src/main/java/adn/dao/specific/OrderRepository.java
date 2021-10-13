/**
 * 
 */
package adn.dao.specific;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import adn.dao.generic.GenericRepository;
import adn.model.entities.Order;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Order;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class OrderRepository {

	private final GenericRepository genericRepository;

	public OrderRepository(GenericRepository genericRepository) {
		super();
		this.genericRepository = genericRepository;
	}

	// @formatter:off
	public Optional<Object[]> findPendingPaymentOrder(String customerId, Collection<String> columns) {
		return genericRepository.findOne(Order.class, columns, (root, query, builder) -> builder.and(
				builder.equal(root.get(_Order.customer).get(_Customer.id), customerId),
				builder.equal(root.get(_Order.status), OrderStatus.PENDING_PAYMENT)));
	}
	// @formatter:on
}
