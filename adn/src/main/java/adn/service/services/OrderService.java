/**
 * 
 */
package adn.service.services;

import static adn.helpers.Base32.crockfords;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;

import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.dao.generic.GenericRepository;
import adn.dao.specific.ItemRepository;
import adn.dao.specific.OrderRepository;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Item;
import adn.model.entities.Order;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.constants.OrderStatus;
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

	private final SessionFactoryImplementor sessionFactory;
	private final GenericRepository genericRepository;
	private final GenericCRUDServiceImpl genericCRUDService;
	private final OrderRepository orderRepository;
	private final ItemRepository itemRepository;

	private final TaskScheduler taskScheduler;

	private static final List<String> ORDER_ID_AND_STATUS = Arrays.asList(_Item.id, _Item.status);

	private static final String RELEASING_ITEMS_TEMPLATE = "Releasing items [%s] from order [%s]";
	private static final String ORDER_NOT_FOUND_TEMPLATE = Common.notfound("Order [%s]");
	private static final String IGNORED_EXPIRATION_PROCESS_TEMPLATE = String
			.format("Ignoring order [%s] since the order's status isn't", "%s", OrderStatus.PENDING_PAYMENT);
	private static final String UNAVAILABLE_ITEMS_TEMPLATE = "The following items are not available: %s";
	private static final String PENDING_PAYMENT_EXISTS_TEMPLATE = "You have a pending-payment order: %s. Please kindly finish this payment before making a new order.";
	private static final String EXPIRATION_PROCESSOR_ASSIGNMENT_TEMPLATE = "Assigning one new processor for expiration of order: [%s]";

	private volatile static Duration ORDER_EXPIRATION_TIME = Duration.ofSeconds(5);

	public OrderService(GenericCRUDServiceImpl genericCRUDService, OrderRepository orderRepository,
			ItemRepository itemRepository, TaskScheduler taskScheduler, SessionFactoryImplementor sessionFactory,
			GenericRepository genericRepository) {
		super();
		this.sessionFactory = sessionFactory;
		this.genericCRUDService = genericCRUDService;
		this.genericRepository = genericRepository;
		this.orderRepository = orderRepository;
		this.itemRepository = itemRepository;
		this.taskScheduler = taskScheduler;
	}

	public Result<Order> createOrder(Order order, boolean flushOnFinish) {
		Session session = ContextProvider.getCurrentSession();

		HibernateHelper.useManualSession();

		Optional<Object[]> pendingPaymentOrder = orderRepository
				.findPendingPaymentOrder(ContextProvider.getPrincipalName(), Arrays.asList(_Order.code, _Order.status));

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

		genericRepository.findAll(Item.class, ORDER_ID_AND_STATUS,
				(root, query, builder) -> builder.in(root.get(_Item.id)).value(itemIds), LockModeType.PESSIMISTIC_WRITE)
				.stream().forEach(cols -> {
					if (((ItemStatus) cols[1]) != ItemStatus.AVAILABLE) {
						unavailableItemIds.add((BigInteger) cols[0]);
					}
				});

		if (!unavailableItemIds.isEmpty()) {
			return Result.bad(String.format(UNAVAILABLE_ITEMS_TEMPLATE, StringHelper.join(unavailableItemIds)));
		}

		Result<Integer> itemUpdates = itemRepository.updateItemsStatus(itemIds, ItemStatus.RESERVED);

		if (!itemUpdates.isOk()) {
			return Result.failed(itemUpdates.getMessagesAsString());
		}

		BigInteger orderId = order.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(AbstractEntityBuilder.CODE_GENERATION_MESSAGE, orderId));
		}

		order.setCode(crockfords.format(orderId));
		session.save(order);

		taskScheduler.schedule(new OrderExpirationProcessor(orderId),
				LocalDateTime.now().plus(ORDER_EXPIRATION_TIME).atZone(ZoneId.systemDefault()).toInstant());

		return genericCRUDService.finish(Result.ok(order), flushOnFinish);
	}

	private class OrderExpirationProcessor implements Runnable {

		private static final String ORDER_UPDATE_FAILURE_TEMPLATE = "Unable to update order: [%s] with message: [%s]";
		private static final String ITEM_UPDATES_FAILURE_TEMPLATE = "Unable to release the following items: [%s] with messages: [%s]";

		private final BigInteger orderId;

		public OrderExpirationProcessor(BigInteger orderId) {
			super();
			this.orderId = orderId;

			if (logger.isDebugEnabled()) {
				logger.debug(String.format(EXPIRATION_PROCESSOR_ASSIGNMENT_TEMPLATE, orderId));
			}
		}

		@Override
		public void run() {
			StatelessSession session = sessionFactory.openStatelessSession();

			session.beginTransaction();

			try {
				Optional<Object[]> optional = genericRepository.findById(Order.class, orderId,
						Arrays.asList(_Order.status), LockModeType.PESSIMISTIC_WRITE, session);

				if (optional.isEmpty()) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format(ORDER_NOT_FOUND_TEMPLATE, orderId));
					}

					return;
				}

				Object[] orderCols = optional.get();

				if (orderCols[0] != OrderStatus.PENDING_PAYMENT) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format(IGNORED_EXPIRATION_PROCESS_TEMPLATE, orderId));
					}

					return;
				}
				// lock the items
				List<BigInteger> itemIds = genericRepository
						.findAll(Item.class, Arrays.asList(_Item.id),
								(root, query, builder) -> builder.equal(root.join(_Item.orders).get(_Order.id),
										orderId),
								LockModeType.PESSIMISTIC_WRITE, session)
						.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
				// all of the items should be RESERVED here or else there's a huge fraud
				if (logger.isDebugEnabled()) {
					logger.debug(String.format(RELEASING_ITEMS_TEMPLATE, StringHelper.join(itemIds), orderId));
				}

				Result<Integer> itemUpdates = itemRepository.updateItemsStatus(itemIds, ItemStatus.AVAILABLE, session);

				if (!itemUpdates.isOk()) {
					if (logger.isErrorEnabled()) {
						logger.error(ITEM_UPDATES_FAILURE_TEMPLATE, StringHelper.join(itemIds),
								itemUpdates.getMessagesAsString());
					}
				}

				Result<Integer> orderUpdate = genericRepository.update(Order.class,
						(root, query, builder) -> query.set(root.get(_Order.status), OrderStatus.EXPIRED),
						(root, query, builder) -> builder.equal(root.get(_Order.id), orderId), session);

				if (!orderUpdate.isOk()) {
					if (logger.isErrorEnabled()) {
						logger.error(String.format(ORDER_UPDATE_FAILURE_TEMPLATE, orderId,
								orderUpdate.getMessagesAsString()));
					}
				}

				session.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
				session.getTransaction().rollback();
			} finally {
				session.close();
			}
		}

	}

}
