/**
 * 
 */
package adn.controller;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.CartItem;
import adn.service.services.ProductService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/customer")
public class RestCustomerController extends BaseController {

	private static final Map<String, String> CART_UPDATED_SUCCESSFULLY = Common.message("Cart updated successfully");

	private final ProductService productService;

	public RestCustomerController(ProductService productService) {
		this.productService = productService;
	}

	@PatchMapping("/cart")
	@Secured(CUSTOMER)
	@Transactional
	public ResponseEntity<?> patchCart(@RequestBody List<CartItem> cartItems) throws UnauthorizedCredential {
		return send(productService.updateCart(cartItems, true), CART_UPDATED_SUCCESSFULLY);
	}

}
