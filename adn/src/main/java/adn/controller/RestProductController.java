/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static adn.service.DepartmentCredential.SALE_CREDENTIAL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

import adn.application.context.ContextProvider;
import adn.controller.query.specification.ProductQuery;
import adn.dao.generic.ResultBatch;
import adn.helpers.StringHelper;
import adn.model.entities.Category;
import adn.model.entities.Product;
import adn.model.entities.StockDetail;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.StockDetailBatch;
import adn.service.internal.ResourceService;
import adn.service.internal.Service.Status;
import adn.service.services.CategoryService;
import adn.service.services.DepartmentService;
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
	public RestProductController(DepartmentService departmentService, ProductService productService,
			ResourceService resourceService, CategoryService categoryService) {
		super(departmentService, productService, resourceService);
		this.categoryService = categoryService;
	}

	@GetMapping(path = "/count", produces = APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductCount() {
		return makeStaleWhileRevalidate(
				ResponseEntity.ok(
						productService.countWithActiveCheck(Product.class, getPrincipalCredential(), SALE_CREDENTIAL)),
				2, TimeUnit.DAYS, 7, TimeUnit.DAYS);
	}

	@GetMapping(path = "/{productId}", produces = APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainProduct(@PathVariable(name = "productId", required = true) String productId,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return send(productService.findWithActiveCheck(productId, Product.class, columns, getPrincipalCredential(),
				SALE_CREDENTIAL), String.format("Product %s not found", productId));
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductsByCategory(
			@RequestParam(name = "category", required = false, defaultValue = "") String categoryId,
			@RequestParam(name = "by", required = false, defaultValue = "") String identifierName,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (StringHelper.hasLength(categoryId)) {
			return ok(productService.getProductsByCategory(categoryId, identifierName, columns, paging,
					getPrincipalCredential(), SALE_CREDENTIAL));
		}

		return ok(productService.readWithActiveCheck(Product.class, columns, paging, getPrincipalCredential(),
				SALE_CREDENTIAL));
	}

	@GetMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	public ResponseEntity<?> searchForProducts(ProductQuery query,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (query.isEmpty()) {
			return sendBadRequest(INVALID_SEARCH_CRITERIA);
		}

		return ResponseEntity
				.ok(productService.searchProduct(columns, paging, query, getPrincipalCredential(), SALE_CREDENTIAL));
	}

	@PostMapping(path = "/category", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> createCategory(@RequestBody Category category) {
		departmentService.assertSaleDepartment();
		// we dont't have to check for id here since it will be generated by
		// IdentifierGenerator and service layer
		return send(categoryService.createCategory(category, true));
	}

	@PutMapping(path = "/category", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> updateCategory(@RequestBody Category model) {
		departmentService.assertSaleDepartment();

		Category persistence = baseRepository.findById(model.getId(), Category.class);

		if (persistence == null) {
			return sendNotFound(String.format("Category %s not found", model.getId()));
		}

		return send(crudService.update(persistence.getId(), model, Category.class, true));
	}

	@GetMapping("/category/list")
	@Transactional(readOnly = true)
	@Secured("ROLE_PERSONNEL")
	public ResponseEntity<?> getCategoryList(@PageableDefault(size = 5) Pageable pageable,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return send(crudService.read(Category.class, columns, pageable, getPrincipalCredential()), null);
	}

	@GetMapping("/category/all")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getAllCategories() throws NoSuchFieldException, UnauthorizedCredential {
		return makeStaleWhileRevalidate(
				categoryService.readWithActiveCheck(Category.class, Arrays.asList("id", "name"),
						PageRequest.of(0, 1000), getPrincipalCredential(), SALE_CREDENTIAL),
				1, TimeUnit.DAYS, 2, TimeUnit.DAYS);
	}

	@GetMapping("/category/count")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCategoryCount() {
		return makeStaleWhileRevalidate(
				categoryService.countWithActiveCheck(Category.class, getPrincipalCredential(), SALE_CREDENTIAL), 1,
				TimeUnit.DAYS, 3, TimeUnit.DAYS);
	}

	@PatchMapping("/category/activation")
	@Transactional
	public ResponseEntity<?> deactivateCategory(@RequestParam(name = "id", required = true) String categoryId,
			@RequestParam(name = "active", required = true) Boolean requestedActiveState) {
		departmentService.assertSaleDepartment();

		Category category = baseRepository.findById(categoryId, Category.class);
		// we use AUTO-FLUSH here
		category.setActive(requestedActiveState);

		if (requestedActiveState == false) {
			category.setDeactivatedDate(LocalDateTime.now());
			category.setUpdatedBy(ContextProvider.getPrincipalName());
		}

		return send(String.format("Modified activation state of category %s", categoryId), null);
	}

	@PostMapping("/stockdetail")
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> createStockDetails(@RequestBody(required = true) StockDetailBatch batch) {
		departmentService.assertStockDepartment();

		ResultBatch<StockDetail> results = crudService.createBatch(batch.getDetails(), StockDetail.class, true);

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
			return sendBadRequest(
					results.getResults().stream().map(result -> result.getMessages()).collect(Collectors.toList()));
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FAILED);
	}

}
