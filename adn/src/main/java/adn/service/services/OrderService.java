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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;

import org.hibernate.Session;
import org.hibernate.SharedSessionContract;
import org.hibernate.StatelessSession;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.TaskScheduler;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.dao.generic.GenericRepository;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.Item;
import adn.model.entities.Operator;
import adn.model.entities.Order;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.metadata._Customer;
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
	public static final String SCHEDULER_NAME = "OrderServiceTaskScheduler";

	private final SessionFactoryImplementor sessionFactory;
	private final GenericRepository genericRepository;
	private final GenericCRUDServiceImpl genericCRUDService;
	private final AuthenticationService authService;

	private final TaskScheduler taskScheduler;

	private static final List<String> ORDER_ID_AND_STATUS = Arrays.asList(_Item.id, _Item.status);

	private static final String RELEASING_ITEMS_TEMPLATE = "Releasing items [%s] from order [%s]";
	private static final String ORDER_NOT_FOUND_TEMPLATE = Common.notfound("Order [%s]");
	private static final String IGNORED_EXPIRATION_PROCESS_TEMPLATE = String
			.format("Ignoring order [%s] since the order's status isn't", "%s", OrderStatus.PENDING_PAYMENT);
	private static final String UNAVAILABLE_ITEMS_TEMPLATE = "The following items are not available: %s";
	private static final String PENDING_PAYMENT_EXISTS_TEMPLATE = "You have a pending-payment order: %s. Please kindly finish this payment before making a new order.";
	private static final String EXPIRATION_PROCESSOR_ASSIGNMENT_TEMPLATE = "Assigning one new processor for expiration of order: [%s]";
	private static final String NOT_PENDING_PAYMENT_TEMPLATE = String
			.format("Current status of the order is %s, not %s", "%s", OrderStatus.PENDING_PAYMENT);
	private static final String UNABLE_TO_PROCESS_ITEMS = "Unable to process some of the items";
	private static final String UNABLE_TO_UPDATE_ORDER_STATUS = "Unable to update order status";

	private volatile static Duration ORDER_EXPIRATION_TIME = Duration.ofDays(2);

	// @formatter:off
	public OrderService(
			GenericCRUDServiceImpl genericCRUDService,
			@Qualifier(SCHEDULER_NAME) TaskScheduler taskScheduler,
			SessionFactoryImplementor sessionFactory,
			GenericRepository genericRepository,
			AuthenticationService authService) {
		super();
		this.sessionFactory = sessionFactory;
		this.genericCRUDService = genericCRUDService;
		this.genericRepository = genericRepository;
		this.taskScheduler = taskScheduler;
		this.authService = authService;
	}
	// @formatter:on
	public Result<Order> createOrder(Order order, boolean flushOnFinish) {
		Session session = ContextProvider.getCurrentSession();

		HibernateHelper.useManualSession();

		Optional<Object[]> pendingPaymentOrder = genericRepository.findOne(Order.class,
				Arrays.asList(_Order.code, _Order.status),
				(root, query, builder) -> builder.and(
						builder.equal(root.get(_Order.customer).get(_Customer.id), ContextProvider.getPrincipalName()),
						builder.equal(root.get(_Order.status), OrderStatus.PENDING_PAYMENT)));

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

		Result<Integer> itemUpdates = updateItemsStatus(itemIds, ItemStatus.RESERVED, session);

		if (!itemUpdates.isOk()) {
			return Result.failed(itemUpdates.getMessagesAsString());
		}

		BigInteger orderId = order.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(AbstractEntityBuilder.CODE_GENERATION_MESSAGE, orderId));
		}

		order.setCode(crockfords.format(orderId));
		session.save(order);

		scheduleExpiration(orderId, ORDER_EXPIRATION_TIME);

		return genericCRUDService.finish(Result.ok(order), flushOnFinish);
	}

	private Result<Integer> updateItemsStatus(Collection<BigInteger> itemIds, ItemStatus status) {
		return updateItemsStatus(itemIds, status, ContextProvider.getCurrentSession());
	}

	public Result<Integer> updateItemsStatus(Collection<BigInteger> itemIds, ItemStatus status,
			SharedSessionContract session) {
		return genericRepository.update(Item.class, (root, query, builder) -> query.set(root.get(_Item.status), status),
				(root, query, builder) -> builder.in(root.get(_Item.id)).value(itemIds), session);
	}

	public ScheduledFuture<?> scheduleExpiration(BigInteger orderId, Duration duration) {
		LocalDateTime executionTimeStamp = LocalDateTime.now().plus(duration);

		if (logger.isDebugEnabled()) {
			logger.info(String.format("Scheduled an expiration of order: [%s] at [%s]", orderId,
					Utils.localDateTime(executionTimeStamp)));
		}

		return taskScheduler.schedule(new OrderExpirationProcessor(orderId),
				executionTimeStamp.atZone(ZoneId.systemDefault()).toInstant());
	}

	public Result<Integer> confirmPayment(BigInteger orderId, boolean flushOnFinish) {
		HibernateHelper.useManualSession();
		// assumes that the order exists
		Object[] order = genericRepository
				.findById(Order.class, orderId, ORDER_ID_AND_STATUS, LockModeType.PESSIMISTIC_WRITE).get();

		if (order[1] != OrderStatus.PENDING_PAYMENT) {
			return Result.bad(String.format(NOT_PENDING_PAYMENT_TEMPLATE, order[1]));
		}

		List<BigInteger> items = genericRepository
				.findAll(Item.class, Arrays.asList(_Item.id), itemsOfOrder(orderId), LockModeType.PESSIMISTIC_WRITE)
				.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
		Result<Integer> itemUpdates = updateItemsStatus(items, ItemStatus.SOLD);

		if (itemUpdates.getInstance() != items.size()) {
			return Result.failed(UNABLE_TO_PROCESS_ITEMS);
		}

		Result<Integer> orderUpdate = genericRepository.update(Order.class, (root, query, builder) -> {
			Operator operator = authService.getOperator();

			return query.set(root.get(_Order.status), OrderStatus.PAID)
					.set(root.get(_Order.updatedTimestamp), LocalDateTime.now())
					.set(root.get(_Order.updatedBy), operator).set(root.get(_Order.handledBy), operator);
		}, (root, query, builder) -> builder.equal(root.get(_Order.id), orderId));

		if (!orderUpdate.isOk()) {
			return Result.failed(UNABLE_TO_UPDATE_ORDER_STATUS);
		}

		return genericCRUDService.finish(orderUpdate, flushOnFinish);
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
						.findAll(Item.class, Arrays.asList(_Item.id), itemsOfOrder(orderId),
								LockModeType.PESSIMISTIC_WRITE, session)
						.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
				// all of the items should be RESERVED here or else there's a huge fraud
				if (logger.isDebugEnabled()) {
					logger.debug(String.format(RELEASING_ITEMS_TEMPLATE, StringHelper.join(itemIds), orderId));
				}

				Result<Integer> itemUpdates = updateItemsStatus(itemIds, ItemStatus.AVAILABLE, session);

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

	private static Specification<Item> itemsOfOrder(BigInteger orderId) {
		return (root, query, builder) -> builder.equal(root.join(_Item.orders).get(_Order.id), orderId);
	}

	public Duration getOrderExpirationTime() {
		return ORDER_EXPIRATION_TIME;
	}

}
