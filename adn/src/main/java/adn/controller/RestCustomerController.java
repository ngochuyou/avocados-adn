/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalName;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.helpers.CollectionHelper;
import adn.model.entities.metadata._Item;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.CartItem;
import adn.service.services.UserService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/customer")
public class RestCustomerController extends BaseController {

	private static final Map<String, String> CART_UPDATED_SUCCESSFULLY = Common.message("Cart updated successfully");
	private static final Map<String, String> UNKNOWN_ACTION = Common.message("Unknown action");

	private final UserService userService;

	public RestCustomerController(UserService userService) {
		this.userService = userService;
	}

	private static final String ADD_ACTION = "add";
	private static final String REMOVE_ACTION = "remove";
	private static final String EMPTY_ACTION = "empty";

	@GetMapping("/cart")
	@Secured(CUSTOMER)
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCustomerCart(
			@RequestParam(name = "productColumns", required = false, defaultValue = "") List<String> productColumns,
			@RequestParam(name = "itemColumns", required = false, defaultValue = "") List<String> itemColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return makeStaleWhileRevalidate(
				userService.readCustomerCart(productColumns, itemColumns, ContextProvider.getPrincipalCredential()), 5,
				TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@PutMapping("/cart/{action}")
	@Secured(CUSTOMER)
	@Transactional
	public ResponseEntity<?> patchCart(@PathVariable(name = "action") String action, CartItem cartItem,
			@RequestParam(name = "items", required = false) List<BigInteger> itemIds,
			@RequestParam(name = "color", required = false) String color) throws UnauthorizedCredential {
		switch (action) {
			case ADD_ACTION: {
				return doAddToCart(cartItem);
			}
			case REMOVE_ACTION: {
				return doRemoveFromCart(itemIds);
			}
			case EMPTY_ACTION: {
				// auto-flush here
				return send(userService.emptyCart(true), CART_UPDATED_SUCCESSFULLY);
			}
			default:
				return bad(UNKNOWN_ACTION);
		}
	}

	private static final String EMPTY_QTY = Common.notEmpty("Quantity");
	private static final String EMPTY_PRODUCT = Common.notEmpty("Product information");

	private ResponseEntity<?> doAddToCart(CartItem cartItem) {
		Result<List<BigInteger>> result = new Result<List<BigInteger>>(null);

		if (cartItem.getProductId() == null) {
			result.bad(_Item.product, EMPTY_PRODUCT);
		}

		if (cartItem.getQuantity() == null) {
			result.bad(CartItem._quantity, EMPTY_QTY);
		}

		if (!result.isOk()) {
			return send(result);
		}

		return send(userService.addToCart(cartItem, getPrincipalName()));
	}

	private ResponseEntity<?> doRemoveFromCart(List<BigInteger> itemIds) {
		if (CollectionHelper.isEmpty(itemIds)) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(null);
		}

		return send(userService.removeFromCart(itemIds, getPrincipalName()), CART_UPDATED_SUCCESSFULLY);
	}

}
