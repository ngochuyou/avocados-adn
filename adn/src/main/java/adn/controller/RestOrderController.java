/**
 * 
 */
package adn.controller;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.application.Result;
import adn.model.entities.Order;
import adn.model.entities.constants.OrderStatus;
import adn.service.services.AuthenticationService;
import adn.service.services.OrderService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/order")
public class RestOrderController extends BaseController {

	private final OrderService orderService;
	private final AuthenticationService authService;

	private static final String SUCCESSFULLY_CONFIRM_PAYMENT = "Successfully confirmed payment";
	private static final String SUCCESSFULLY_UPDATE_STATUS = "Successfully updated order status";
	private static final String ORDER_ID_TEMPLATE = "Order %s";
	private static final String STATUS_NOT_ALLOWED = "Status %s is not allowed";

	public RestOrderController(OrderService orderService, AuthenticationService authService) {
		super();
		this.orderService = orderService;
		this.authService = authService;
	}

	@PostMapping
	@Transactional
	@Secured(CUSTOMER)
	public ResponseEntity<?> createOrder(@RequestBody Order newOrder) throws Exception {
		return sendAndProduce(orderService.createOrder(newOrder, true));
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
			@PathVariable(name = "status") int statusCode) {
		OrderStatus requestedStatus;

		try {
			requestedStatus = OrderStatus.of(statusCode);
		} catch (IllegalArgumentException iae) {
			return bad(Common.message(iae.getMessage()));
		}

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
