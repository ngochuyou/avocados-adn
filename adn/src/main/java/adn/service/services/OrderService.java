/**
 * 
 */
package adn.service.services;

import static adn.helpers.Base32.crockfords;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.dao.specific.ItemRepository;
import adn.dao.specific.OrderRepository;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Order;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Order;
import adn.service.entity.builder.AbstractEntityBuilder;
import adn.service.internal.Service;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class OrderService implements Service {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	private final GenericCRUDServiceImpl genericCRUDService;
	private final OrderRepository orderRepository;
	private final ItemRepository itemRepository;

	private static final String UNAVAILABLE_ITEMS_TEMPLATE = "The following items are not available: %s";
	private static final String PENDING_PAYMENT_EXISTS_TEMPLATE = "You have a pending-payment order: %s. Please kindly finish this payment before making a new order.";

	public OrderService(GenericCRUDServiceImpl genericCRUDService, OrderRepository orderRepository,
			ItemRepository itemRepository) {
		super();
		this.genericCRUDService = genericCRUDService;
		this.orderRepository = orderRepository;
		this.itemRepository = itemRepository;
	}

	public Result<Order> createOrder(Order order, boolean flushOnFinish) {
		Session session = ContextProvider.getCurrentSession();
		HibernateHelper.useManualSession();

		Optional<Object[]> pendingPaymentOrder = orderRepository
				.findPendingPaymentOrder(ContextProvider.getPrincipalName(), Arrays.asList(_Order.code));

		if (pendingPaymentOrder.isPresent()) {
			return Result.bad(String.format(PENDING_PAYMENT_EXISTS_TEMPLATE, pendingPaymentOrder.get()[0]));
		}

		Result<Order> firstPhaseResult = genericCRUDService.create(null, order, Order.class, false);

		if (!firstPhaseResult.isOk()) {
			return firstPhaseResult;
		}

		order = firstPhaseResult.getInstance();

		List<BigInteger> unavailableItemIds = new ArrayList<>(order.getItems().size());
		List<BigInteger> itemIds = order.getItems().stream().map(item -> item.getId()).collect(Collectors.toList());

		itemRepository.findAllItemsAndLock(itemIds, Arrays.asList(_Item.id, _Item.status)).stream().forEach(cols -> {
			if (((ItemStatus) cols[1]) != ItemStatus.AVAILABLE) {
				unavailableItemIds.add((BigInteger) cols[0]);
			}
		});

		if (!unavailableItemIds.isEmpty()) {
			return Result.bad(String.format(UNAVAILABLE_ITEMS_TEMPLATE, StringHelper.join(unavailableItemIds)));
		}

		if (itemRepository.updateItemsStatus(itemIds, ItemStatus.UNAVAILABLE) != itemIds.size()) {
			return Result.failed("Failed to place order, some of the items were unavailable");
		}

		BigInteger orderId = order.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(AbstractEntityBuilder.CODE_GENERATION_MESSAGE, orderId));
		}

		order.setCode(crockfords.format(orderId));
		session.save(order);

		return genericCRUDService.finish(Result.ok(order), flushOnFinish);
	}

}
