/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getCurrentSession;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.helpers.CollectionHelper;
import adn.helpers.HibernateHelper;
import adn.model.entities.Category;
import adn.model.entities.Order;
import adn.model.entities.OrderDetail;
import adn.model.entities.Product;
import adn.model.entities.ProductCost;
import adn.model.entities.Provider;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Entity;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._NamedResource;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._OrderDetail;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;

/**
 * This controller is meant for temporary usage only, very wacky stuffs here
 * 
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/stats")
public class RestStatisticController extends BaseController {

	private static final String KEY_TOTAL = "total";
	private static final String KEY_YEAR = Common.YEAR.toLowerCase();
	private static final String KEY_MONTH = Common.MONTH.toLowerCase();
	private static final String KEY_DAY = Common.DAY.toLowerCase();

	private static final Logger logger = LoggerFactory.getLogger(RestStatisticController.class);

	@GetMapping("/cost/percategory")
	@Transactional(readOnly = true)
	@Secured(HEAD)
	public ResponseEntity<?> getProvidersCountByCategories(@RequestParam(name = "categories") List<Long> categoryIds) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<ProductCost> costRoot = cq.from(ProductCost.class);
		Join<Product, Category> categoryJoin = costRoot.join(_ProductCost.product).join(_Product.category);
		Path<Object> categoryIdPath = categoryJoin.get(_Category.id);
		// @formatter:off
		cq.multiselect(
				categoryJoin.get(_Category.id), // category id
				categoryJoin.get(_Category.name), // category name
				builder.countDistinct(costRoot.get(_ProductCost.id).get(_ProductCost.providerId))) // providers count
			.where(builder.in(categoryIdPath).value(categoryIds))
			.groupBy(categoryIdPath);
		// @formatter:on
		Query<Tuple> hql = session.createQuery(cq);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		List<Map<String, Object>> stats = HibernateHelper.toRows(hql.list()).stream()
				.map(cols -> Map.of("categoryId", cols[0], "categoryName", cols[1], "providersCount", cols[2]))
				.collect(Collectors.toList());

		return makeStaleWhileRevalidate(stats, 30, TimeUnit.DAYS, 31, TimeUnit.DAYS);
	}

	@GetMapping("/cost/perprovider/{product}")
	@Transactional(readOnly = true)
	@Secured(HEAD)
	public ResponseEntity<?> getProductCostsByProviders(@PathVariable(name = "product") BigInteger productId,
			@RequestParam(name = "providers") List<UUID> providerIds,
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<ProductCost> costRoot = cq.from(ProductCost.class);
		Join<ProductCost, Product> productJoin = costRoot.join(_ProductCost.product);
		Join<ProductCost, Provider> providerJoin = costRoot.join(_ProductCost.provider);
		// @formatter:off
		Path<Object> productIdPath = productJoin.get(_Product.id);
		Path<Object> providerIdPath = providerJoin.get(_Provider.id);
		cq.multiselect(
				productIdPath, productJoin.get(_Product.name),
				providerIdPath, providerJoin.get(_Provider.name),
				builder.function("ROUND", BigDecimal.class, builder.avg(costRoot.get(_ProductCost.cost)), builder.literal(4)))
			.where(builder.and(
					builder.equal(costRoot.get(_ProductCost.active), true),
					builder.equal(productIdPath, productId)),
					builder.in(providerIdPath).value(providerIds),
					builder.isNotNull(costRoot.get(_ProductCost.approvalInformations).get(_ProductCost.approvedTimestamp)),
					new Supplier<Predicate>() {

						@Override
						public Predicate get() {
							if (year != null) {
								Path<Object> appliedTimetampPath = costRoot.get(_ProductCost.id).get(_ProductCost.appliedTimestamp);
								
								return builder.and(
										builder.equal(builder.function("YEAR", Integer.class, appliedTimetampPath), year),
										Optional.ofNullable(month)
											.map(wrappedMonth -> builder.equal(builder.function("MONTH", Integer.class, appliedTimetampPath), wrappedMonth))
											.orElse(builder.conjunction()));
							}
							
							return builder.conjunction();
						}
						
					}.get())
			.groupBy(productIdPath, providerIdPath);
		// @formatter:on
		Query<Tuple> hql = session.createQuery(cq);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}
		// @formatter:off
		String productName = "productName";
		String providerName = "providerName";
		String costKey = "avgCost";
		
		List<Map<String, Object>> stats = HibernateHelper.toRows(hql.list()).stream()
				.map(cols -> {
					return Map.of(
							_ProductCost.productId, cols[0],
							productName, cols[1],
							_ProductCost.providerId, cols[2],
							providerName, cols[3],
							costKey, cols[4]);
				})
				.collect(Collectors.toList());
		// @formatter:on
		return makeStaleWhileRevalidate(stats, 30, TimeUnit.DAYS, 31, TimeUnit.DAYS);
	}

	private static final String COUNT = "COUNT";
//	private static final String SUM = "SUM";

	@GetMapping("/product/total")
	@Transactional(readOnly = true)
	@Secured(HEAD)
	public ResponseEntity<?> takeActionOnProductsSold(
			@RequestParam(name = "categories", required = false, defaultValue = "") List<Long> categoryIds,
			@RequestParam(name = "products", required = false, defaultValue = "") List<BigInteger> productIds,
			@RequestParam(name = "overall", required = false, defaultValue = "true") Boolean overall,
			@RequestParam(name = "year", required = false) Integer year,
			@RequestParam(name = "month", required = false) Integer month,
			@RequestParam(name = "sort", required = false, defaultValue = "ASC") Direction direction,
			@RequestParam(name = "action", required = false, defaultValue = COUNT) String action) {
		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		Function<Root<OrderDetail>, Expression<?>> actionProducer = action.equals(COUNT)
				? root -> builder.count(builder.literal(1))
				: root -> builder.sum(root.get(_OrderDetail.price));

		if (overall) {
			return takeActionOnAllTimeTotalProductsSold(session, builder, categoryIds, productIds, actionProducer);
		}

		return takeActionOnTotalProductsSoldPerTemporal(session, builder, year, month, direction, categoryIds,
				productIds, actionProducer);
	}

	private ResponseEntity<?> takeActionOnAllTimeTotalProductsSold(Session session, CriteriaBuilder builder,
			List<Long> categoryIds, List<BigInteger> productIds,
			Function<Root<OrderDetail>, Expression<?>> actionProducer) {
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<OrderDetail> root = cq.from(OrderDetail.class);

		Predicate commonPredicate = builder.and(builder.equal(root.get(_OrderDetail.active), true),
				builder.notEqual(root.join(_OrderDetail.order).get(_Order.status), OrderStatus.PENDING_PAYMENT));
		boolean byCategories = !CollectionHelper.isEmpty(categoryIds);
		boolean byProducts = !CollectionHelper.isEmpty(productIds);
		Expression<?> action = actionProducer.apply(root);

		if (!byProducts && !byCategories) {
			cq.multiselect(action).where(commonPredicate);

			return makeStaleWhileRevalidate(Map.of(KEY_TOTAL, makeQuery(session, cq).getSingleResult().get(0)), 12,
					TimeUnit.HOURS, 24, TimeUnit.HOURS);
		}

		Join<OrderDetail, Product> productJoin = root.join(_OrderDetail.item).join(_Item.product);
		Join<OrderDetail, ?> join = byCategories ? productJoin.join(_Product.category) : productJoin;
		Path<Object> joinIdPath = join.get(_Entity.id);

		cq.multiselect(joinIdPath, join.get(_NamedResource.name), action).where(
				builder.and(commonPredicate, builder.in(joinIdPath).value(byCategories ? categoryIds : productIds)))
				.groupBy(joinIdPath);

		// @formatter:off
		return makeStaleWhileRevalidate(makeQuery(session, cq).list()
				.stream()
					.map(byCategories ?
							tuple -> Map.of(
								_Category.id, tuple.get(0),
								_Category.name, tuple.get(1),
								KEY_TOTAL, tuple.get(2)) :
							tuple -> Map.of(
								_Product.id, tuple.get(0),
								_Product.name, tuple.get(1),
								KEY_TOTAL, tuple.get(2)))
					.collect(Collectors.toList()),
				12, TimeUnit.HOURS,
				24, TimeUnit.HOURS);
		// @formatter:on
	}

	private Query<Tuple> makeQuery(Session session, CriteriaQuery<Tuple> cq) {
		Query<Tuple> hql = session.createQuery(cq);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return hql;
	}

	private ResponseEntity<?> takeActionOnTotalProductsSoldPerTemporal(Session session, CriteriaBuilder builder,
			Integer year, Integer month, Direction direction, List<Long> categoryIds, List<BigInteger> productIds,
			Function<Root<OrderDetail>, Expression<?>> actionProducer) {
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<OrderDetail> root = cq.from(OrderDetail.class);
		Join<OrderDetail, Order> orderJoin = root.join(_OrderDetail.order);
		Expression<?>[] temporalExpressions = (Expression<?>[]) Array.newInstance(Expression.class, 3);

		temporalExpressions[0] = HibernateHelper.year(builder, orderJoin.get(_Order.createdTimestamp));
		temporalExpressions[1] = HibernateHelper.month(builder, orderJoin.get(_Order.createdTimestamp));
		temporalExpressions[2] = HibernateHelper.day(builder, orderJoin.get(_Order.createdTimestamp));

		int temporalKey = getTemporalKey(year, month);
		Join<OrderDetail, ?> associationJoin = null;

		boolean byCategories = !CollectionHelper.isEmpty(categoryIds);
		boolean byProducts = !CollectionHelper.isEmpty(productIds);

		if (byProducts || byCategories) {
			Join<OrderDetail, Product> productJoin = root.join(_OrderDetail.item).join(_Item.product);

			associationJoin = byCategories ? productJoin.join(_Product.category) : productJoin;
		}
		// @formatter:off
		cq.multiselect(PRODUCTS_SOLD_PER_TEMPORAL_SELECTIONS_RESOLVERS.get(temporalKey).get(temporalExpressions, actionProducer.apply(root), associationJoin))
			.where(builder.and(
					builder.equal(root.get(_OrderDetail.active), true)),
					builder.notEqual(orderJoin.get(_Order.status), OrderStatus.PENDING_PAYMENT),
					PRODUCTS_SOLD_PER_TEMPORAL_PREDICATE_RESOLVERS
						.get(temporalKey).get(temporalExpressions, year, month, builder),
					associationJoin == null ? 
							builder.conjunction() : 
								builder.in(associationJoin.get(_Entity.id)).value(byCategories ? categoryIds : productIds))
			.groupBy(PRODUCTS_SOLD_PER_TEMPORAL_GROUPS_RESOLVERS.get(temporalKey).get(temporalExpressions, associationJoin))
			.orderBy(PRODUCTS_SOLD_PER_TEMPORAL_SORT_RESOLVERS.get(temporalKey).get(temporalExpressions, direction, builder));
		// @formatter:on
		Query<Tuple> hql = session.createQuery(cq);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return makeStaleWhileRevalidate(
				PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS.get(temporalKey).get(hql.list(), associationJoin), 12,
				TimeUnit.HOURS, 24, TimeUnit.HOURS);
	}

	private int getTemporalKey(Integer year, Integer month) {
		if (year == null) {
			return 0;
		}

		if (month == null) {
			return 1;
		}

		return 2;
	}

	// @formatter:off
	private static final Map<Integer, ProductSoldPerTemporalSelectionsResolver> PRODUCTS_SOLD_PER_TEMPORAL_SELECTIONS_RESOLVERS = Map
			.of(
					0, (temporalExpressions, action, assoJoin) -> Optional.ofNullable(assoJoin).map(join -> Arrays.asList((Selection<?>) join.get(_Entity.id), join.get(_NamedResource.name), temporalExpressions[0], action)).orElse(Arrays.asList(temporalExpressions[0], action)),
					1, (temporalExpressions, action, assoJoin) -> Optional.ofNullable(assoJoin).map(join -> Arrays.asList((Selection<?>) join.get(_Entity.id), join.get(_NamedResource.name), temporalExpressions[1], action)).orElse(Arrays.asList(temporalExpressions[1], action)),
					2, (temporalExpressions, action, assoJoin) -> Optional.ofNullable(assoJoin).map(join -> Arrays.asList((Selection<?>) join.get(_Entity.id), join.get(_NamedResource.name), temporalExpressions[2], action)).orElse(Arrays.asList(temporalExpressions[2], action)));
	
	private static final Map<Integer, ProductSoldPerTemporalGroupResolver> PRODUCTS_SOLD_PER_TEMPORAL_GROUPS_RESOLVERS = Map
			.of(
					0, (temporalExpressions, assoJoin) -> Optional.ofNullable(assoJoin).map(join -> Arrays.asList(temporalExpressions[0], join.get(_Entity.id))).orElse(Arrays.asList(temporalExpressions[0])),
					1, (temporalExpressions, assoJoin) -> Optional.ofNullable(assoJoin).map(join -> Arrays.asList(temporalExpressions[1], join.get(_Entity.id))).orElse(Arrays.asList(temporalExpressions[1])),
					2, (temporalExpressions, assoJoin) -> Optional.ofNullable(assoJoin).map(join -> Arrays.asList(temporalExpressions[2], join.get(_Entity.id))).orElse(Arrays.asList(temporalExpressions[2])));
	
	private static final Map<Integer, ProductSoldPerTemporalSortResolver> PRODUCTS_SOLD_PER_TEMPORAL_SORT_RESOLVERS = Map
			.of(
					0, (temporalExpressions, direction, builder) -> Arrays.asList(direction == Direction.ASC ? builder.asc(temporalExpressions[0]) : builder.desc(temporalExpressions[0])),
					1, (temporalExpressions, direction, builder) -> Arrays.asList(direction == Direction.ASC ? builder.asc(temporalExpressions[1]) : builder.desc(temporalExpressions[1])),
					2, (temporalExpressions, direction, builder) -> Arrays.asList(direction == Direction.ASC ? builder.asc(temporalExpressions[2]) : builder.desc(temporalExpressions[2])));
	
	private static final Map<Integer, ProductSoldPerTemporalPredicateResolver> PRODUCTS_SOLD_PER_TEMPORAL_PREDICATE_RESOLVERS = Map
			.of(
					0, (temporalExpressions, year, month, builder) -> builder.conjunction(),
					1, (temporalExpressions, year, month, builder) -> builder.equal(temporalExpressions[0], year),
					2, (temporalExpressions, year, month, builder) -> builder.and(builder.equal(temporalExpressions[0], year), builder.equal(temporalExpressions[1], month)));
	
	private static final Map<Boolean, Map<Integer, Function<Tuple, Map<String, Object>>>> PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS_RESOLVERS;
	
	static {
		Map<Integer, Function<Tuple, Map<String, Object>>> nonJoinResolvers = Map.of(
				0, tuple -> Map.of(KEY_YEAR, tuple.get(0), KEY_TOTAL, tuple.get(1)),
				1, tuple -> Map.of(KEY_MONTH, tuple.get(0), KEY_TOTAL, tuple.get(1)),
				2, tuple -> Map.of(KEY_DAY, tuple.get(0), KEY_TOTAL, tuple.get(1)));
		
		String assoId = "associationId";
		String assoName = "associationName";
		
		Map<Integer, Function<Tuple, Map<String, Object>>> joinedResolvers = Map.of(
				0, tuple -> Map.of(
						assoId, tuple.get(0),
						assoName, tuple.get(1),
						KEY_YEAR, tuple.get(2),
						KEY_TOTAL, tuple.get(3)),
				1, tuple -> Map.of(
						assoId, tuple.get(0),
						assoName, tuple.get(1),
						KEY_MONTH, tuple.get(2),
						KEY_TOTAL, tuple.get(3)),
				2, tuple -> Map.of(
						assoId, tuple.get(0),
						assoName, tuple.get(1),
						KEY_DAY, tuple.get(2),
						KEY_TOTAL, tuple.get(3)));

		PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS_RESOLVERS = Collections.unmodifiableMap(Map.of(
				true, nonJoinResolvers,
				false, joinedResolvers));
	}
	
	private static final Map<Integer, ProductSoldPerTemporalQueryTransformer> PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS = Map
			.of(
					0, (tuples, assoJoin) -> tuples.stream()
							.map(PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS_RESOLVERS
									.get(assoJoin == null).get(0))
							.collect(Collectors.toList()),
					1, (tuples, assoJoin) -> tuples.stream()
							.map(PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS_RESOLVERS
									.get(assoJoin == null).get(1))
							.collect(Collectors.toList()),
					2, (tuples, assoJoin) -> tuples.stream()
							.map(PRODUCTS_SOLD_PER_TEMPORAL_QUERY_TRANSFORMERS_RESOLVERS
									.get(assoJoin == null).get(2))
							.collect(Collectors.toList()));
	// @formatter:on
	private interface ProductSoldPerTemporalSelectionsResolver {

		List<Selection<?>> get(Expression<?>[] temporalExpressions, Expression<?> actionExpression,
				Join<OrderDetail, ?> assoJoin);

	}

	private interface ProductSoldPerTemporalPredicateResolver {

		Predicate get(Expression<?>[] temporalExpressions, Integer year, Integer month, CriteriaBuilder builder);

	}

	private interface ProductSoldPerTemporalGroupResolver {

		List<Expression<?>> get(Expression<?>[] temporalExpressions, Join<OrderDetail, ?> assoJoin);

	}

	private interface ProductSoldPerTemporalSortResolver {

		List<javax.persistence.criteria.Order> get(Expression<?>[] temporalExpressions, Direction direction,
				CriteriaBuilder builder);

	}

	private interface ProductSoldPerTemporalQueryTransformer {

		List<Map<String, ?>> get(List<Tuple> tuples, Join<OrderDetail, ?> assoJoin);

	}

}
