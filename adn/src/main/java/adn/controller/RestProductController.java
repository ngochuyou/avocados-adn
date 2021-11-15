/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static org.springframework.http.ResponseEntity.ok;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
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
import adn.application.context.builders.DepartmentScopeContext;
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.ResultBatch;
import adn.dao.specific.ProductPriceRepository;
import adn.dao.specific.ProductRepository;
import adn.helpers.CollectionHelper;
import adn.model.entities.Category;
import adn.model.entities.Item;
import adn.model.entities.Product;
import adn.model.entities.ProductPrice;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductPrice;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.ItemBatch;
import adn.model.models.ProductPriceModel;
import adn.service.internal.ResourceService;
import adn.service.internal.Service.Status;
import adn.service.services.AuthenticationService;
import adn.service.services.CategoryService;
import adn.service.services.ProductService;
import adn.service.services.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/product")
public class RestProductController extends ProductController {

	private final CategoryService categoryService;
	private final ProductPriceRepository priceRepository;
	private final ProductRepository productRepository;

	private static final Map<String, String> ITEMS_BATCH_WAS_EMPTY = Common.message("Item batch was empty");

	@Autowired
	public RestProductController(AuthenticationService authService, ProductService productService,
			ResourceService resourceService, CategoryService categoryService, ProductPriceRepository priceRepository,
			ProductRepository productRepository) {
		super(authService, productService, resourceService);
		this.categoryService = categoryService;
		this.priceRepository = priceRepository;
		this.productRepository = productRepository;
	}

	@GetMapping("/count")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductCount() {
		return makeStaleWhileRevalidate(genericRepository.count(Product.class), 1, TimeUnit.MINUTES, 5,
				TimeUnit.MINUTES);
	}

	@GetMapping("/{productId}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainProduct(@PathVariable(name = "productId", required = true) BigInteger productId,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		Map<String, Object> result = crudService.readById(productId, Product.class, columns, getPrincipalCredential());

		return makeStaleWhileRevalidate(
				Optional.<Object>ofNullable(result)
						.orElse(Common.notfound(String.format(Common.COMMON_TEMPLATE, "Product", productId))),
				5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProducts(
			@RequestParam(name = "ids", required = false, defaultValue = "") List<BigInteger> productIds,
			@RequestParam(name = "category", required = false) Serializable categoryIdentifier,
			@RequestParam(name = "by", required = false) String categoryIdentifierName, ProductQuery restQuery,
			@PageableDefault(size = 10, sort = _Product.createdDate, direction = Direction.DESC) Pageable paging)
			throws Exception {
		Set<String> requestedColumns = restQuery.getColumns();

		if (!CollectionHelper.isEmpty(productIds)) {
			return ok(crudService.readAll(Product.class, requestedColumns,
					(root, query, builder) -> builder.in(root.get(_Product.id)).value(productIds), paging,
					getPrincipalCredential()));
		}

		Specification<Product> spec = (root, query, builder) -> Optional.ofNullable(restQuery.getName())
				.map(like -> builder.like(root.get(_Product.name), like.getLike())).orElse(builder.conjunction());

		return ok(productService.readOnSaleProducts(categoryIdentifier, categoryIdentifierName, requestedColumns, spec,
				paging, getPrincipalCredential()));
	}

	@GetMapping("/internal")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> getInternalProducts(
			@RequestParam(name = "category", required = false) Serializable categoryIdentifier,
			@RequestParam(name = "by", required = false) String categoryIdentifierName, ProductQuery restQuery,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws Exception {
		Specification<Product> spec = (root, query, builder) -> Optional.ofNullable(restQuery.getName())
				.map(like -> builder.like(root.get(_Product.name), like.getLike())).orElse(builder.conjunction());

		return ok(productService.readAllProducts(categoryIdentifier, categoryIdentifierName, columns, spec, paging,
				getPrincipalCredential()));
	}

	@PatchMapping("/approve/{productId}")
	@Transactional
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> approveProduct(@PathVariable(name = "productId") BigInteger productId) throws Exception {
		authService.assertSaleDepartment();

		Optional<Product> persistence = genericRepository.findById(Product.class, productId);

		if (persistence.isEmpty()) {
			return notFound(Map.of(Common.MESSAGE,
					Common.notfound(String.format(Common.COMMON_TEMPLATE, "Product", productId))));
		}

		return sendAndProduce(genericService.approve(Product.class, productId, true));
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
			return notFound(Map.of(Common.MESSAGE,
					Common.notfound(String.format(Common.COMMON_TEMPLATE, "Product", productId))));
		}

		return sendAndProduce(productService.changeLockState(productId, lockState, true));
	}

	private static final Specification<Product> PRODUCT_IS_NOT_LOCKED = (root, query, builder) -> builder
			.equal(root.get(_Product.locked), false);

	@GetMapping("/search")
	@Transactional(readOnly = true)
	public ResponseEntity<?> searchForProducts(ProductQuery restQuery,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (!restQuery.hasCriteria()) {
			return bad(Common.INVALID_SEARCH_CRITERIA);
		}

		List<Map<String, Object>> rows = crudService.readAll(Product.class, columns,
				SpecificationFactory.hasNameLike(restQuery).and(PRODUCT_IS_NOT_LOCKED), getPrincipalCredential());

		return makeStaleWhileRevalidate(rows, 5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@GetMapping("/search/internal")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> searchForInternalProducts(ProductQuery restQuery,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		if (!restQuery.hasCriteria()) {
			return bad(Common.message(Common.INVALID_SEARCH_CRITERIA));
		}

		authService.assertDepartment(DepartmentScopeContext.customerService(), DepartmentScopeContext.sale(),
				DepartmentScopeContext.stock());

		List<Map<String, Object>> rows = crudService.readAll(Product.class, columns,
				SpecificationFactory.hasNameLike(restQuery), getPrincipalCredential());

		return makeStaleWhileRevalidate(rows, 5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@GetMapping("/price")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductPrices(@RequestParam(name = "ids") List<BigInteger> productIds)
			throws Exception {
		List<Object[]> prices = priceRepository.findAllCurrents(productIds,
				Arrays.asList(_ProductPrice.productId, _ProductPrice.price));

		return makeStaleWhileRevalidate(prices.stream().collect(HashMap<Object, Object>::new,
				(map, cols) -> map.put(cols[0], cols[1]), HashMap::putAll), 1, TimeUnit.MINUTES, 5, TimeUnit.MINUTES);
	}

	@GetMapping("/price/{productId}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductPrice(@PathVariable(name = "productId") BigInteger productId,
			@RequestParam(name = "from", required = false) @DateTimeFormat(pattern = Common.COMMON_LDT_FORMAT) LocalDateTime from,
			@RequestParam(name = "to", required = false) @DateTimeFormat(pattern = Common.COMMON_LDT_FORMAT) LocalDateTime to,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(direction = Direction.ASC, sort = _ProductPrice.appliedTimestamp) Pageable paging)
			throws Exception {
		return makeStaleWhileRevalidate(crudService.readAll(ProductPrice.class, columns,
				resolveProductPriceTimestamp(from, to).and((root, query, builder) -> builder
						.equal(root.get(_ProductPrice.id).get(_ProductPrice.productId), productId)),
				paging, getPrincipalCredential()), 5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	private Specification<ProductPrice> resolveProductPriceTimestamp(LocalDateTime from, LocalDateTime to) {
		boolean isRanged = from != null && to != null;

		if (isRanged) {
			return (root, query, builder) -> builder
					.between(root.get(_ProductPrice.id).get(_ProductPrice.appliedTimestamp), from, to);
		}

		return (root, query, builder) -> Optional.ofNullable(from)
				.map(startTimestamp -> builder.greaterThanOrEqualTo(
						root.get(_ProductPrice.id).get(_ProductPrice.appliedTimestamp), startTimestamp))
				.orElse(builder.conjunction());
	}

	@PostMapping("/price")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> submitProductPrice(@RequestBody ProductPriceModel model) throws Exception {
		authService.assertSaleDepartment();

		ProductPrice price = extract(ProductPrice.class, model, new ProductPrice());

		return sendAndProduce(productService.createProductPrice(price, true));
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

		return sendAndProduce(genericService.approve(ProductPrice.class, persistenceId, true));
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
		return sendAndProduce(categoryService.createCategory(category, true));
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

		return sendAndProduce(crudService.update(optional.get().getId(), model, Category.class, true));
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

	@PostMapping("/items")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createItemBatch(@RequestBody ItemBatch batch) {
		authService.assertStockDepartment();

		Collection<Item> items = batch.getItems();

		if (CollectionHelper.isEmpty(items)) {
			return bad(ITEMS_BATCH_WAS_EMPTY);
		}

		ResultBatch<Item> resultBatch = productService.createItemsBatch(items, true);

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

	private static final String QUANTITY = "quantity";

	@GetMapping(value = "/items/{productId}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getItemsListByProduct(@PathVariable(name = "productId") BigInteger productId,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		int countColumnsIndex = columns.size();
		List<Object[]> rows = productRepository.findItemsByProduct(productId, columns,
				(root, query, builder) -> builder.equal(root.get(_Item.status), ItemStatus.AVAILABLE), true);
		List<Map<String, Object>> items = crudService.readAll(Item.class, columns, getPrincipalCredential(),
				(metadata) -> rows);

		IntStream.range(0, rows.size())
				.forEach(index -> items.get(index).put(QUANTITY, rows.get(index)[countColumnsIndex]));

		return makeStaleWhileRevalidate(items, 5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@GetMapping(value = "/items")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getItemsList(@RequestParam(name = "ids") List<BigInteger> itemIds,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		return ok(crudService.readAll(Item.class, columns,
				(root, query, builder) -> builder.in(root.get(_Item.id)).value(itemIds), getPrincipalCredential()));
	}

}
