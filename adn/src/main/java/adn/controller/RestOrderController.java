/**
 * 
 */
package adn.controller;

import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adn.model.entities.Order;
import adn.service.services.OrderService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/order")
public class RestOrderController extends BaseController {

	private final OrderService orderService;

	public RestOrderController(OrderService orderService) {
		super();
		this.orderService = orderService;
	}

	@PostMapping
	@Transactional
	@Secured(CUSTOMER)
	public ResponseEntity<?> createOrder(@RequestBody Order newOrder) throws Exception {
		return send(orderService.createOrder(newOrder, true));
	}

}
