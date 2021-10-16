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
import adn.service.internal.Service.Status;
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
	
	private static final String SUCCESS_CONFIRM = "Successfully confirmed payment";
	private static final String ORDER_ID_TEMPLATE = "Order %s";

	public RestOrderController(OrderService orderService, AuthenticationService authService) {
		super();
		this.orderService = orderService;
		this.authService = authService;
	}

	@PostMapping
	@Transactional
	@Secured(CUSTOMER)
	public ResponseEntity<?> createOrder(@RequestBody Order newOrder) throws Exception {
		return send(orderService.createOrder(newOrder, true));
	}

	@PatchMapping("/confirm/{orderId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> confirmOrderPayment(@PathVariable(name = "orderId") BigInteger orderId) {
		authService.assertCustomerServiceDepartment();

		if (genericRepository.countById(Order.class, orderId) == 0) {
			return notFound(Common.notfound(String.format(ORDER_ID_TEMPLATE, orderId)));
		}

		Result<Integer> result = orderService.confirmPayment(orderId, true);

		if (result.isOk()) {
			return ResponseEntity.ok(Common.message(SUCCESS_CONFIRM));
		}

		return result.getStatus() == Status.BAD ? bad(result.getMessages()) : fails(result.getMessages());
	}

}
