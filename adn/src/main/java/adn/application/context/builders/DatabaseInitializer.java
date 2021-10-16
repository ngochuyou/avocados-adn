/**
 * 
 */
package adn.application.context.builders;

import static adn.application.context.builders.DepartmentScopeContext.CUSTOMERSERVICE_NAME;
import static adn.application.context.builders.DepartmentScopeContext.PERSONNEL_NAME;
import static adn.application.context.builders.DepartmentScopeContext.SALE_NAME;
import static adn.application.context.builders.DepartmentScopeContext.STOCK_NAME;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.dao.generic.GenericRepository;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Department;
import adn.model.entities.Head;
import adn.model.entities.Item;
import adn.model.entities.Order;
import adn.model.entities.constants.Gender;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._NamedResource;
import adn.model.entities.metadata._Order;
import adn.service.services.OrderService;
import adn.service.services.UserService;

/**
 * @author Ngoc Huy
 *
 */
public class DatabaseInitializer implements ContextBuilder {

	@Override
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		SessionFactory sessionFactory = ContextProvider.getBean(SessionFactory.class);
		Session session = sessionFactory.openSession();

		session.setHibernateFlushMode(FlushMode.MANUAL);
		session.beginTransaction();

		logger.info("Building " + this.getClass().getName());

		try {
			insertHead(session);
			insertDepartments(session);
			processOrders(session);

			session.flush();
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.clear();
		} finally {
			session.close();
		}

		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void insertDepartments(Session session) {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		GenericRepository genericRepository = ContextProvider.getBean(GenericRepository.class);
		Department department;
		Function<String, Specification> departmentHasName = name -> (root, query, builder) -> builder
				.equal(root.get(_NamedResource.name), name);

		if (genericRepository.findOne(Department.class, departmentHasName.apply(STOCK_NAME), session).isEmpty()) {
			logger.info("Inserting " + STOCK_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(STOCK_NAME);
			session.save(department);
		}

		if (genericRepository.findOne(Department.class, departmentHasName.apply(SALE_NAME), session).isEmpty()) {
			logger.info("Inserting " + SALE_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(SALE_NAME);
			session.save(department);
		}

		if (genericRepository.findOne(Department.class, departmentHasName.apply(PERSONNEL_NAME), session).isEmpty()) {
			logger.info("Inserting " + PERSONNEL_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(PERSONNEL_NAME);
			session.save(department);
		}

		if (genericRepository.findOne(Department.class, departmentHasName.apply(CUSTOMERSERVICE_NAME), session)
				.isEmpty()) {
			logger.info("Inserting " + CUSTOMERSERVICE_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(CUSTOMERSERVICE_NAME);
			session.save(department);
		}
	}

	private void insertHead(Session session) {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		PasswordEncoder passwordEncoder = ContextProvider.getBean(PasswordEncoder.class);

		if (session.get(Head.class, "ngochuy.ou") == null) {
			Head head = new Head("ngochuy.ou");

			head.setPassword(passwordEncoder.encode("password"));
			head.setActive(true);
			head.setEmail("ngochuy.ou@gmail.com");
			head.setFirstName("Tran");
			head.setGender(Gender.MALE);
			head.setLastName("Vu Ngoc Huy");
			head.setPhone("0974032706");
			head.setPhoto(UserService.DEFAULT_ACCOUNT_PHOTO_NAME);
			head.setLocked(Boolean.FALSE);

			session.save(head);

			logger.info("Inserting HEAD: " + head.getId());
		}
	}

	private void processOrders(Session session) {
		processExpiredOrders(session);
		resumeScheduledOrders(session);
	}

	private void processExpiredOrders(Session session) {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		OrderService orderService = ContextProvider.getBean(OrderService.class);
		Duration expirationDuration = orderService.getOrderExpirationTime();
		GenericRepository genericRepository = ContextProvider.getBean(GenericRepository.class);
		List<BigInteger> expiredPendingPaymentOrders = genericRepository
				.findAll(Order.class, Arrays.asList(_Order.id), (root, query, builder) -> builder.and(
						builder.greaterThanOrEqualTo(builder
								.abs(builder.function("timestampdiff", Long.class, HibernateHelper.MySQLUnit.SECOND,
										builder.literal(LocalDateTime.now()), root.get(_Order.createdTimestamp))),
								expirationDuration.toSeconds()),
						builder.equal(root.get(_Order.status), OrderStatus.PENDING_PAYMENT)),
						LockModeType.PESSIMISTIC_WRITE, session)
				.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());

		if (expiredPendingPaymentOrders.isEmpty()) {
			return;
		}

		logger.info(String.format("Marking orders [%s] as [%s]", StringHelper.join(expiredPendingPaymentOrders),
				OrderStatus.EXPIRED));

		Result<Integer> orderUpdates = genericRepository.update(Order.class,
				(root, query, builder) -> query.set(root.get(_Order.status), OrderStatus.EXPIRED),
				(root, query, builder) -> builder.in(root.get(_Order.id)).value(expiredPendingPaymentOrders), session);

		if (!orderUpdates.isOk()) {
			throw new StaleStateException(String.format("Unable to mark the following orders as %s: [%s]",
					OrderStatus.EXPIRED, StringHelper.join(expiredPendingPaymentOrders)));
		}

		Set<BigInteger> statusIgnoredItems = new HashSet<>();

		for (BigInteger orderId : expiredPendingPaymentOrders) {
			List<Object[]> items = genericRepository.findAll(Item.class, Arrays.asList(_Item.id, _Item.status),
					(root, query, builder) -> builder.equal(root.join(_Item.orders).get(_Order.id), orderId),
					LockModeType.PESSIMISTIC_WRITE, session);

			for (Object[] row : items) {
				if (!statusIgnoredItems.contains(row[0]) && row[1] != ItemStatus.RESERVED) {
					throw new StaleStateException(String.format("Unexpected item state: [%s] in item %s. Expected [%s]",
							row[1], row[0], ItemStatus.RESERVED));
				}
			}

			List<BigInteger> itemIds = items.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());

			logger.info(String.format("Releasing items [%s] from order [%s]", StringHelper.join(itemIds), orderId));

			Result<Integer> itemUpdates = orderService.updateItemsStatus(itemIds, ItemStatus.AVAILABLE, session);

			if (!itemUpdates.isOk()) {
				throw new StaleStateException(
						String.format("Unable to releasing the following items from order [%s]: [%s]. %s",
								StringHelper.join(itemIds), orderId, itemUpdates.getMessagesAsString()));
			}

			statusIgnoredItems.addAll(itemIds);
		}
	}

	private void resumeScheduledOrders(Session session) {
		OrderService orderService = ContextProvider.getBean(OrderService.class);
		Duration expirationDuration = orderService.getOrderExpirationTime();
		GenericRepository genericRepository = ContextProvider.getBean(GenericRepository.class);
		List<Object[]> scheduledOrders = genericRepository
				.findAll(Order.class, Arrays.asList(_Order.id, _Order.createdTimestamp),
						(root, query, builder) -> builder.and(
								builder.equal(root.get(_Order.status), OrderStatus.PENDING_PAYMENT),
								builder.lessThan(builder.abs(builder.function("timestampdiff", Long.class,
										HibernateHelper.MySQLUnit.SECOND, builder.literal(LocalDateTime.now()),
										root.get(_Order.createdTimestamp))), expirationDuration.toSeconds())),
						LockModeType.PESSIMISTIC_WRITE, session);
		LocalDateTime now = LocalDateTime.now();

		scheduledOrders.stream().forEach(cols -> {
			BigInteger orderId = (BigInteger) cols[0];
			LocalDateTime createdTimestamp = (LocalDateTime) cols[1];

			orderService.scheduleExpiration(orderId, Duration.between(now, createdTimestamp.plus(expirationDuration)));
		});
	}

}
