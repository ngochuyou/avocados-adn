/**
 * 
 */
package adn.controller;

import static adn.service.services.DepartmentScopedService.assertDepartment;
import static adn.service.services.DepartmentScopedService.stock;

import java.sql.SQLSyntaxErrorException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.model.entities.Category;
import adn.service.services.DepartmentService;
import adn.service.services.ProductService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
public class RestProductController extends DepartmentScopedController {

	private final ProductService productService;

	@Autowired
	public RestProductController(DepartmentService departmentService, ProductService productService) {
		super(departmentService);
		this.productService = productService;
	}

	protected void assertStockDepartment() {
		assertDepartment(getPrincipalDepartment(), stock());
	}

	@PostMapping(path = "/rest/category", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> createCategory(@RequestBody Category category) {
		assertStockDepartment();
		// we dont't have to check for id here since it will be generated by
		// IdentifierGenerator and service layer
		return send(productService.createCategory(category, true));
	}

	@PutMapping(path = "/rest/category", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> updateCategory(@RequestBody Category model) {
		assertStockDepartment();

		Category persistence = baseRepository.findById(model.getId(), Category.class);

		if (persistence == null) {
			return sendNotFound(String.format("Category %s not found", model.getId()));
		}

		return send(crudService.update(persistence.getId(), model, Category.class, true));
	}

	@GetMapping("/rest/category/list")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCategoryList(@PageableDefault(size = 5) Pageable pageable,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws SQLSyntaxErrorException {
		return send(crudService.read(Category.class, columns.toArray(new String[columns.size()]), pageable), null);
	}

	@GetMapping("/rest/category/count")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCategoryCount() {
		return makeStaleWhileRevalidate(baseRepository.count(Category.class), 1, TimeUnit.DAYS, 3, TimeUnit.DAYS);
	}

	@PatchMapping("/rest/category/activation")
	@Transactional
	public ResponseEntity<?> deactivateCategory(@RequestParam(name = "id", required = true) String categoryId,
			@RequestParam(name = "active", required = true) Boolean requestedActiveState) {
		assertStockDepartment();

		Category category = baseRepository.findById(categoryId, Category.class);
		// we use AUTO-FLUSH here
		category.setActive(requestedActiveState);
		
		if (requestedActiveState == false) {
			category.setDeactivatedDate(LocalDateTime.now());
			category.setUpdatedBy(ContextProvider.getPrincipalName());
		}
		
		return send(String.format("Modified activation state of category %s", categoryId), null);
	}

}
