/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static adn.application.context.builders.CredentialFactory.owner;
import static org.springframework.http.ResponseEntity.ok;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.Join;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.application.context.builders.CredentialFactory;
import adn.model.entities.Customer;
import adn.model.entities.Order;
import adn.model.entities.OrderDetail;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._OrderDetail;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.DeliveryInstructions;
import adn.service.services.AuthenticationService;
import adn.service.services.OrderService;
import adn.service.services.UserService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/order")
public class RestOrderController extends BaseController {

	private final OrderService orderService;
	private final UserService userService;
	private final AuthenticationService authService;

	private static final String SUCCESSFULLY_CONFIRM_PAYMENT = "Successfully confirmed payment";
	private static final String SUCCESSFULLY_UPDATE_STATUS = "Successfully updated order status";
	private static final String ORDER_ID_TEMPLATE = "Order %s";
	private static final String STATUS_NOT_ALLOWED = "Status %s is not allowed";

	public RestOrderController(OrderService orderService, AuthenticationService authService, UserService userService) {
		super();
		this.orderService = orderService;
		this.userService = userService;
		this.authService = authService;
	}

	@GetMapping
	@Transactional(readOnly = true)
	@Secured(CUSTOMER)
	public ResponseEntity<?> getOrdersList(
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 5, sort = _Order.createdTimestamp, direction = Direction.DESC) Pageable paging)
			throws NoSuchFieldException, UnauthorizedCredential {

		return makeStaleWhileRevalidate(
				crudService.readAll(Order.class, columns,
						(root, query, builder) -> builder.equal(root.get(_Order.customer).get(_Customer.id),
								ContextProvider.getPrincipalName()),
						paging, CredentialFactory.owner()),
				3, TimeUnit.SECONDS, 5, TimeUnit.SECONDS);
	}

	@GetMapping("/internal")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> getOrdersList(@RequestParam(name = "customer", required = false) String customerName,
			@RequestParam(name = "status", required = false) OrderStatus orderStatus,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 5) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertCustomerServiceDepartment();
		// @formatter:off
		Specification<Order> spec = (root, query, builder) -> {
			return builder.and(
				Optional.ofNullable(customerName)
					.map(name -> {
						Join<Order, Customer> customerJoin = root.join(_Order.customer);
						String nameExpression = Common.like(customerName);
						return builder.or(
								builder.like(customerJoin.get(_Customer.firstName), nameExpression),
								builder.like(customerJoin.get(_Customer.lastName), nameExpression));
					}).orElse(builder.conjunction()),
				Optional.ofNullable(orderStatus)
					.map(status -> builder.equal(root.get(_Order.status), status)).orElse(builder.conjunction()));
		};
		// @formatter:on
		return makeStaleWhileRevalidate(
				crudService.readAll(Order.class, columns, spec, paging, getPrincipalCredential()), 3, TimeUnit.SECONDS,
				5, TimeUnit.SECONDS);
	}

	@PostMapping
	@Transactional
	@Secured(CUSTOMER)
	public ResponseEntity<?> createOrder(@RequestBody DeliveryInstructions deliveryInstructions) throws Exception {
		Result<Order> phaseOne = orderService.createOrder(ContextProvider.getPrincipalName(), deliveryInstructions,
				false);

		if (!phaseOne.isOk()) {
			return send(phaseOne);
		}

		return send(userService.emptyCart(true), produce(phaseOne.getInstance(), Order.class, owner()));
	}

	@GetMapping("/{orderCode}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainOrder(@PathVariable(name = "orderCode") String orderCode,
			@RequestParam(name = "orderColumns", required = false, defaultValue = "") HashSet<String> orderColumns,
			@RequestParam(name = "detailsColumns", required = false, defaultValue = "") List<String> detailsColumns)
			throws Exception {
		if (!orderColumns.contains(_Order.details)) {
			return ok(crudService.read(Order.class, orderColumns,
					(root, query, builder) -> builder.equal(root.get(_Order.code), orderCode), owner()));
		}

		orderColumns.remove(_Order.details);

		Map<String, Object> order = crudService.read(Order.class, orderColumns,
				(root, query, builder) -> builder.equal(root.get(_Order.code), orderCode), owner());
		BigInteger orderId = (BigInteger) order.get(_Order.id);

		order.put(_Order.details, crudService.readAll(OrderDetail.class, detailsColumns,
				(root, query, builder) -> Optional.ofNullable(orderId)
						.map(id -> builder.equal(root.get(_OrderDetail.id).get(_OrderDetail.orderId), id))
						.orElse(builder.equal(root.join(_OrderDetail.order).get(_Order.code), orderCode)),
				owner()));

		return ok(order);
	}

	@PatchMapping("/confirm/{orderId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> confirmOrderPayment(@PathVariable(name = "orderId") BigInteger orderId) {
		authService.assertCustomerServiceDepartment();

		if (genericRepository.countById(Order.class, orderId) == 0) {
			return notFound(Common.message(Common.notfound(String.format(ORDER_ID_TEMPLATE, orderId))));
		}

		Result<Integer> result = orderService.confirmPayment(orderId, true);

		return send(result.isOk() ? Result.ok(Common.message(SUCCESSFULLY_CONFIRM_PAYMENT)) : result);
	}

	@PatchMapping("/status/{orderId}/{status}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> updateOrderStatus(@PathVariable(name = "orderId") BigInteger orderId,
			@PathVariable(name = "status") OrderStatus requestedStatus) {
		if (requestedStatus != OrderStatus.DELIVERING && requestedStatus != OrderStatus.FINISHED) {
			return bad(Common.message(String.format(STATUS_NOT_ALLOWED, requestedStatus)));
		}

		if (genericRepository.countById(Order.class, orderId) == 0) {
			return notFound(Common.message(Common.notfound(String.format(ORDER_ID_TEMPLATE, orderId))));
		}

		Result<Integer> result = orderService.updateStatus(orderId, requestedStatus, true);

		return send(result.isOk() ? Result.ok(Common.message(SUCCESSFULLY_UPDATE_STATUS)) : result);
	}

}
