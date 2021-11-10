/**
 * 
 */
package adn.application.context.builders;

import static adn.application.context.builders.DepartmentScopeContext.CUSTOMERSERVICE_NAME;
import static adn.application.context.builders.DepartmentScopeContext.PERSONNEL_NAME;
import static adn.application.context.builders.DepartmentScopeContext.SALE_NAME;
import static adn.application.context.builders.DepartmentScopeContext.STOCK_NAME;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.LockModeType;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.RandomUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.dao.generic.GenericRepository;
import adn.dao.specific.ProductPriceRepository;
import adn.helpers.Base32;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.Customer;
import adn.model.entities.Department;
import adn.model.entities.District;
import adn.model.entities.Head;
import adn.model.entities.Item;
import adn.model.entities.Order;
import adn.model.entities.OrderDetail;
import adn.model.entities.Product;
import adn.model.entities.ProductCost;
import adn.model.entities.ProductPrice;
import adn.model.entities.Provider;
import adn.model.entities.constants.Gender;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.constants.NamedSize;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.id.ProductCostId;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._District;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._NamedResource;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductPrice;
import adn.model.entities.metadata._Provider;
import adn.security.UserDetailsImpl;
import adn.service.internal.GenericCRUDService;
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

//			MockDataInitializer mocker = new MockDataInitializer(session,
//					ContextProvider.getBean(GenericRepository.class),
//					ContextProvider.getBean(GenericCRUDService.class));
//
//			mocker.init();

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

	@SuppressWarnings("unused")
	private class MockDataInitializer {

		private final Session session;
		private final GenericRepository genericRepository;
		private final GenericCRUDService crudService;
		private final Head head = new Head("ngochuy.ou");
		private final Random random = new Random();

		public MockDataInitializer(Session session, GenericRepository genericRepository,
				GenericCRUDService crudService) {
			this.session = session;
			this.genericRepository = genericRepository;
			this.crudService = crudService;
		}

		public void init() {
			head.setLocked(false);
			head.setPassword("password");
			head.setUpdatedDate(LocalDateTime.now());

			SecurityContextHolder.getContext()
					.setAuthentication(new UsernamePasswordAuthenticationToken(new UserDetailsImpl(head), "password"));

//			doPrices();
//			doCosts();
//			doItems();
//			doOrders();
		}

		public void doOrders() {
			Logger logger = LoggerFactory.getLogger(this.getClass());
			String address = "(601) 786-0202. Po Box 974. Fayette, Mississippi(MS), 39069 · (541) 269-1859 97104 Stian Smith Rd Coos Bay, Oregon(OR), 97420";
			List<BigInteger> productIds = genericRepository
					.findAll(Product.class, Arrays.asList(_Product.id),
							Pageable.ofSize(resolveSizeLimit(genericRepository.count(Product.class, session))),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
			List<Integer> districtIds = genericRepository
					.findAll(District.class, Arrays.asList(_District.id), Pageable.ofSize(100),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (Integer) cols[0]).collect(Collectors.toList());
			List<String> customerIds = genericRepository
					.findAll(Customer.class, Arrays.asList(_Customer.id), Pageable.ofSize(100),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (String) cols[0]).collect(Collectors.toList());
			ProductPriceRepository priceRepo = ContextProvider.getBean(ProductPriceRepository.class);

			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Tuple> query;
			Root<Item> itemRoot;

			LocalDateTime start = LocalDateTime.of(2020, 6, 10, 0, 0);
			LocalDateTime end = LocalDateTime.of(2020, 12, 31, 23, 59);
			int outOfStockCount = 0;
			int orderCount = 0;

			outer: while (start.isBefore(end)) {
				for (int i = 0; i < RandomUtils.nextInt(1, 1); i++) {
					try {
						List<BigInteger> selectedProducts = productIds.stream().filter(id -> RandomUtils.nextBoolean())
								.collect(Collectors.toList());

						query = builder.createTupleQuery();
						itemRoot = query.from(Item.class);

						query.multiselect(
								Arrays.asList(itemRoot.get(_Item.id), itemRoot.get(_Item.product).get(_Product.id)))
								.where(builder.and(
										builder.in(itemRoot.join(_Item.product).get(_Product.id))
												.value(selectedProducts),
										builder.equal(itemRoot.get(_Item.status), ItemStatus.AVAILABLE)));

						Query<Tuple> hql = session.createQuery(query);

						hql.setMaxResults(RandomUtils.nextInt(1, 3));

						Map<BigInteger, BigInteger> itemMap = HibernateHelper.toRows(hql.list()).stream()
								.map(cols -> Map.entry((BigInteger) cols[0], (BigInteger) cols[1]))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

						if (itemMap.isEmpty()) {
							logger.info(String.format("out of stock in %s, %s, #%d", start.getMonth(), start.getDayOfMonth(), outOfStockCount));
							outOfStockCount++;

							if (outOfStockCount >= 100) {
								break outer;
							}

							continue;
						}

						Map<BigInteger, BigDecimal> priceMap = priceRepo
								.findAllCurrents(productIds,
										Arrays.asList(_ProductPrice.productId, _ProductPrice.price), session)
								.stream().map(cols -> Map.entry((BigInteger) cols[0], (BigDecimal) cols[1]))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

						Order order = new Order();

						order.setActive(true);
						order.setStatus(OrderStatus.FINISHED);
						order.setAddress(address.substring(0, RandomUtils.nextInt(0, address.length() - 1)));
						order.setDistrict(new District(districtIds.get(RandomUtils.nextInt(0, districtIds.size()))));
						order.setCustomer(new Customer(customerIds.get(RandomUtils.nextInt(0, customerIds.size()))));
						order.setCreatedTimestamp(start);
						order.setUpdatedTimestamp(start);

						session.persist(order);

						order.setCode(Base32.crockfords.format(order.getId()));

						itemMap.entrySet().stream().forEach(entry -> {
							OrderDetail detail = new OrderDetail(order.getId(), entry.getKey(),
									priceMap.get(entry.getValue()), true);

							session.save(detail);
							logger.info(detail.toString());
						});

						genericRepository.update(Item.class,
								(root, updateQuery, updateBuilder) -> updateQuery.set(root.get(_Item.status),
										ItemStatus.SOLD),
								(root, itemQuery, itemBuilder) -> itemBuilder.in(root.get(_Item.id))
										.value(itemMap.keySet()),
								session);
						orderCount++;
						logger.info("Order#" + orderCount);
					} catch (Exception e) {
						e.printStackTrace();
						session.clear();
					}
				}

				start = start.plusDays(1);
			}

			session.flush();
		}

		public void doItems() {
			List<BigInteger> productIds = genericRepository
					.findAll(Product.class, Arrays.asList(_Product.id),
							Pageable.ofSize(resolveSizeLimit(genericRepository.count(Product.class, session))),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
			List<UUID> providerIds = genericRepository
					.findAll(Provider.class, Arrays.asList(_Provider.id),
							Pageable.ofSize(resolveSizeLimit(genericRepository.count(Provider.class, session))),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (UUID) cols[0]).collect(Collectors.toList());
			List<String> colors;
			List<NamedSize> sizes;
			Logger logger = LoggerFactory.getLogger(this.getClass());
			NamedSize[] sizeValues = NamedSize.values();
			int itemsCount = 0;

			for (BigInteger productId : productIds) {
				colors = Arrays.asList(getRandomHex(RandomUtils.nextInt(1, 2)));
				sizes = IntStream.range(0, 3)
						.mapToObj(index -> sizeValues[RandomUtils.nextInt(0, sizeValues.length - 1)])
						.collect(Collectors.toList());

				List<Utils.Entry<String, NamedSize>> combinations = distribute(colors, sizes,
						(color, string) -> new Utils.Entry<>(color, string));

				for (Utils.Entry<String, NamedSize> combination : combinations) {
					int n = RandomUtils.nextInt(100, 200);

					for (int i = 0; i < n; i++) {
						try {
							Item item = new Item();

							item.setColor(combination.getKey());
							item.setNamedSize(combination.getValue());
							item.setStatus(ItemStatus.AVAILABLE);
							item.setCost(new BigDecimal(String.valueOf(RandomUtils.nextInt(50000, 150000))));
							item.setProduct(new Product(productId));
							item.setProvider(new Provider(providerIds.get(RandomUtils.nextInt(0, providerIds.size()))));

							logger.info(itemsCount + ": " + item.toString());
							crudService.create(null, item, Item.class, session, true);
							itemsCount++;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}

			session.flush();
		}

		private <R, X, Y> List<R> distribute(List<X> left, List<Y> right, BiFunction<X, Y, R> mapper) {
			return left.stream().flatMap(leftEle -> right.stream().map(rightEle -> mapper.apply(leftEle, rightEle)))
					.collect(Collectors.toList());
		}

		private String[] getRandomHex(int amount) {
			return IntStream.range(0, amount).mapToObj(i -> String.format("#%06x", random.nextInt(0xffffff + 1)))
					.toArray(String[]::new);
		}

		public void doCosts() {
			List<BigInteger> productIds = genericRepository
					.findAll(Product.class, Arrays.asList(_Product.id),
							Pageable.ofSize(resolveSizeLimit(genericRepository.count(Product.class, session))),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
			List<UUID> providerIds = genericRepository
					.findAll(Provider.class, Arrays.asList(_Provider.id), Pageable.ofSize(20),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (UUID) cols[0]).collect(Collectors.toList());

			LocalDateTime start;
			LocalDateTime next;
			LocalDateTime end;

			for (BigInteger productId : productIds) {
				for (UUID providerId : providerIds.stream().filter(ele -> RandomUtils.nextBoolean())
						.collect(Collectors.toList())) {
					start = LocalDateTime.now().minusYears(10);
					end = LocalDateTime.now().plusMonths(5);

					while (start.isBefore(end)) {
						next = start.plusDays(RandomUtils.nextInt(15, 45));

						ProductCost cost = new ProductCost();

						cost.setId(new ProductCostId(productId, providerId, start, next));
						cost.setProduct(new Product(productId));
						cost.setProvider(new Provider(providerId));
						cost.setCost(new BigDecimal(String.valueOf(RandomUtils.nextInt(50000, 150000))));

						try {
							crudService.create(cost.getId(), cost, ProductCost.class, session, true);
						} catch (Exception e) {
							e.printStackTrace();
						}

						cost.setApprovedTimestamp(start.plusDays(RandomUtils.nextInt(1, 7)));
						cost.setApprovedBy(head);

						start = next.plusSeconds(30);
					}
				}
			}
		}

		private int resolveSizeLimit(long size) {
			return (int) (size > Integer.MAX_VALUE ? Integer.MAX_VALUE : size);
		}

		private void doPrices() {
			List<BigInteger> productIds = genericRepository
					.findAll(Product.class, Arrays.asList(_Product.id),
							Pageable.ofSize(resolveSizeLimit(genericRepository.count(Product.class, session))),
							LockModeType.PESSIMISTIC_WRITE, session)
					.stream().map(cols -> (BigInteger) cols[0]).collect(Collectors.toList());
			LocalDateTime start;
			LocalDateTime next;
			LocalDateTime end;
			ProductPrice price;

			for (BigInteger productId : productIds) {
				start = LocalDateTime.now().minusYears(10);
				end = LocalDateTime.now().plusMonths(5);

				while (start.isBefore(end)) {
					next = start.plusDays(RandomUtils.nextInt(15, 45));

					try {
						price = new ProductPrice();

						price.setId(new ProductPriceId(productId, start, next));
						price.setProduct(new Product(productId));
						price.setPrice(new BigDecimal(String.valueOf(RandomUtils.nextInt(450000, 850000))));

						crudService.create(price.getId(), price, ProductPrice.class, session, true);

						price.setApprovedTimestamp(start.plusDays(RandomUtils.nextInt(1, 7)));
						price.setApprovedBy(head);
					} catch (Exception e) {
						e.printStackTrace();
					}

					start = next.plusSeconds(30);
				}
			}
		}

	}

}
