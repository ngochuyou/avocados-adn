/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static org.springframework.http.ResponseEntity.ok;

import java.math.BigInteger;
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
import org.springframework.http.HttpStatus;
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
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.ResultBatch;
import adn.model.entities.Category;
import adn.model.entities.Item;
import adn.model.entities.Product;
import adn.model.entities.metadata._Category;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.StockDetailBatch;
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
		return makeStaleWhileRevalidate(baseRepository.count(Product.class), 2, TimeUnit.DAYS, 7, TimeUnit.DAYS);
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
			@RequestParam(name = "category", required = false, defaultValue = "") Long categoryId,
			@RequestParam(name = "by", required = false, defaultValue = "") String identifierName,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (categoryId != null) {
			return ok(productService.readProductsByCategory(categoryId, identifierName, columns, paging,
					getPrincipalCredential()));
		}

		return ok(crudService.readAll(Product.class, columns, paging, getPrincipalCredential()));
	}

	@PatchMapping("/approve/{productId}")
	@Transactional
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> approveProduct(@PathVariable(name = "productId") BigInteger productId) throws Exception {
		authService.assertSaleDepartment();

		Optional<Product> persistence = baseRepository.findById(Product.class, productId);

		if (persistence.isEmpty()) {
			return sendNotFound(Map.of(Common.MESSAGE, getProductNotFoundMessage(productId)));
		}

		return send(productService.approveProduct(productId, true));
	}

	@GetMapping("/search")
	@Transactional(readOnly = true)
	public ResponseEntity<?> searchForProducts(ProductQuery query,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (!query.hasCriteria()) {
			return sendBad(Common.INVALID_SEARCH_CRITERIA);
		}

		return ResponseEntity.ok(productService.searchProduct(columns, paging, query, getPrincipalCredential()));
	}

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

		Optional<Category> optional = baseRepository.findById(Category.class, model.getId());

		if (optional.isEmpty()) {
			return sendNotFound(String.format("Category %s not found", model.getId()));
		}

		return send(crudService.update(optional.get().getId(), model, Category.class, true));
	}

	@GetMapping("/category/list")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> getCategoryList(@PageableDefault(size = 5) Pageable pageable,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return send(crudService.readAll(Category.class, columns, pageable, getPrincipalCredential()), null);
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
		return makeStaleWhileRevalidate(baseRepository.count(Category.class), 1, TimeUnit.DAYS, 3, TimeUnit.DAYS);
	}

	@PostMapping("/stockdetail")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createStockDetails(@RequestBody(required = true) StockDetailBatch batch) {
		authService.assertStockDepartment();

		ResultBatch<Item> results = crudService.createBatch(batch.getDetails(), Item.class, true);

		if (results.isOk()) {
			return ResponseEntity.ok(results.getResults().stream().map(result -> {
				try {
					return dynamicMapModelFactory.producePojo(result.getInstance(), null, getPrincipalCredential());
				} catch (UnauthorizedCredential e) {
					return e.getMessage();
				}
			}).collect(Collectors.toList()));
		}

		if (results.getStatus() == Status.BAD) {
			return sendBad(
					results.getResults().stream().map(result -> result.getMessages()).collect(Collectors.toList()));
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Common.FAILED);
	}

}
