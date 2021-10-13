/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static org.springframework.http.ResponseEntity.ok;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.application.Result;
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.ResultBatch;
import adn.helpers.CollectionHelper;
import adn.model.entities.Category;
import adn.model.entities.Item;
import adn.model.entities.Product;
import adn.model.entities.ProductPrice;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._Category;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.ItemBatch;
import adn.model.models.ProductPriceModel;
import adn.service.internal.ResourceService;
import adn.service.internal.Service.Status;
import adn.service.services.AuthenticationService;
import adn.service.services.CategoryService;
import adn.service.services.ProductService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/product")
public class RestProductController extends ProductController {

	private final CategoryService categoryService;

	@Autowired
	public RestProductController(AuthenticationService authService, ProductService productService,
			ResourceService resourceService, CategoryService categoryService) {
		super(authService, productService, resourceService);
		this.categoryService = categoryService;
	}

	@GetMapping("/count")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductCount() {
		return makeStaleWhileRevalidate(genericRepository.count(Product.class), 2, TimeUnit.DAYS, 7, TimeUnit.DAYS);
	}

	private String getProductNotFoundMessage(BigInteger productId) {
		return String.format("Product %s not found", productId);
	}

	@GetMapping("/{productId}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainProduct(@PathVariable(name = "productId", required = true) BigInteger productId,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return send(crudService.readById(productId, Product.class, columns, getPrincipalCredential()),
				getProductNotFoundMessage(productId));
	}

	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProducts(
			@RequestParam(name = "category", required = false, defaultValue = "") Long categoryIdentifier,
			@RequestParam(name = "by", required = false, defaultValue = "") String categoryIdentifierName,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws Exception {
		return ok(productService.readOnSaleProducts(categoryIdentifier, categoryIdentifierName, columns, paging,
				getPrincipalCredential()));
	}

	@PatchMapping("/approve/{productId}")
	@Transactional
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> approveProduct(@PathVariable(name = "productId") BigInteger productId) throws Exception {
		authService.assertSaleDepartment();

		Optional<Product> persistence = genericRepository.findById(Product.class, productId);

		if (persistence.isEmpty()) {
			return notFound(Map.of(Common.MESSAGE, getProductNotFoundMessage(productId)));
		}

		return send(genericService.approve(Product.class, productId, true));
	}

	@PatchMapping("/lockstate/{productId}/{lockState}")
	@Transactional
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> changeProductLockState(
	// @formatter:off
			@PathVariable(name = "productId") BigInteger productId,
			@PathVariable(name = "lockState") boolean lockState) throws Exception {
		// @formatter:on
		authService.assertSaleDepartment();

		Optional<Product> persistence = genericRepository.findById(Product.class, productId);

		if (persistence.isEmpty()) {
			return notFound(Map.of(Common.MESSAGE, getProductNotFoundMessage(productId)));
		}

		return send(productService.changeLockState(productId, lockState, true));
	}

	@GetMapping("/search")
	@Transactional(readOnly = true)
	public ResponseEntity<?> searchForProducts(ProductQuery query,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (!query.hasCriteria()) {
			return bad(Common.INVALID_SEARCH_CRITERIA);
		}

		return ResponseEntity.ok(productService.searchProduct(columns, paging, query, getPrincipalCredential()));
	}

	@PostMapping("/price")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> submitProductPrice(@RequestBody ProductPriceModel model) throws Exception {
		authService.assertSaleDepartment();

		ProductPrice price = extract(ProductPrice.class, model, new ProductPrice());

		return send(productService.createProductPrice(price, true));
	}

	@PatchMapping("/price/approve")
	@Secured(HEAD)
	@Transactional
	public ResponseEntity<?> approveProductPrice(@RequestParam(name = "product", required = true) BigInteger productId,
			@RequestParam(name = "applied", required = true) @DateTimeFormat(pattern = Common.COMMON_LDT_FORMAT) LocalDateTime appliedTimestamp,
			@RequestParam(name = "dropped", required = true) @DateTimeFormat(pattern = Common.COMMON_LDT_FORMAT) LocalDateTime droppedTimestamp)
			throws Exception {
		ProductPriceId persistenceId = new ProductPriceId(productId, appliedTimestamp, droppedTimestamp);
		Optional<ProductPrice> persistence = genericRepository.findById(ProductPrice.class, persistenceId);

		if (persistence.isEmpty()) {
			return notFound(Common.notfound(persistenceId));
		}

		return send(genericService.approve(ProductPrice.class, persistenceId, true));
	}

//	@PatchMapping("/price")
//	@Secured({ HEAD, PERSONNEL })
//	@Transactional
//	// @formatter:off
//	public ResponseEntity<?> updateProductPrice(
//			@RequestParam(name = "product", required = true) BigInteger productId,
//			@RequestParam(name = "applied", required = true) LocalDateTime appliedTimestamp,
//			@RequestParam(name = "dropped", required = true) LocalDateTime droppedTimestamp,
//			@RequestBody ProductPriceModel model) {
//	// @formatter:on
//		authService.assertSaleDepartment();
//
//		ProductPriceId persistenceId = new ProductPriceId(productId, appliedTimestamp, droppedTimestamp);
//		Optional<ProductPrice> persistence = genericRepository.findById(ProductPrice.class, persistenceId);
//
//		if (persistence.isEmpty()) {
//			return notFound(Common.notfound(persistenceId));
//		}
//
//		return null;
//	}

	@PostMapping("/category")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createCategory(@RequestBody Category category) throws Exception {
		authService.assertSaleDepartment();
		// we dont't have to check for id here since it will be overridden by
		// IdentifierGenerator and service layer
		return send(categoryService.createCategory(category, true));
	}

	@PutMapping("/category")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> updateCategory(@RequestBody Category model) throws Exception {
		authService.assertSaleDepartment();

		Optional<Category> optional = genericRepository.findById(Category.class, model.getId());

		if (optional.isEmpty()) {
			return notFound(String.format("Category %s not found", model.getId()));
		}

		return send(crudService.update(optional.get().getId(), model, Category.class, true));
	}

	@GetMapping("/category/list")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> getCategoryList(@PageableDefault(size = 5) Pageable pageable,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return ok(crudService.readAll(Category.class, columns, pageable, getPrincipalCredential()));
	}

	@GetMapping("/category/all")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getAllCategories() throws NoSuchFieldException, UnauthorizedCredential {
		return makeStaleWhileRevalidate(crudService.readAll(Category.class, Arrays.asList(_Category.id, _Category.name),
				PageRequest.of(0, 1000), getPrincipalCredential()), 1, TimeUnit.DAYS, 2, TimeUnit.DAYS);
	}

	@GetMapping("/category/count")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCategoryCount() {
		return makeStaleWhileRevalidate(genericRepository.count(Category.class), 1, TimeUnit.DAYS, 3, TimeUnit.DAYS);
	}

	@PostMapping("/item")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createItemBatch(@RequestBody ItemBatch batch) {
		authService.assertStockDepartment();

		ResultBatch<Item> resultBatch = productService.createItemsBatch(batch.getItems(), true);

		if (resultBatch.isOk()) {
			return ResponseEntity.ok(resultBatch.getResults().stream().map(result -> {
				try {
					return produce(result.getInstance(), Item.class, getPrincipalCredential());
				} catch (UnauthorizedCredential e) {
					return e.getMessage();
				}
			}).collect(Collectors.toList()));
		}

		if (resultBatch.getStatus() == Status.BAD) {
			List<Result<Item>> results = resultBatch.getResults();
			
			if (!CollectionHelper.isEmpty(results)) {
				return bad(results.stream().map(Result::getMessages).collect(Collectors.toList()));
			}
			
			return bad(resultBatch.getMessage());
		}

		return fails(Common.error(resultBatch.getMessage()));
	}

}
