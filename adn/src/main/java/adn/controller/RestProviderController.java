/**
 * 
 */
package adn.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.application.context.builders.DepartmentScopeContext;
import adn.controller.query.request.ProviderRequest;
import adn.controller.query.specification.ProviderQuery;
import adn.model.entities.Provider;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.services.DepartmentService;
import adn.service.services.ProviderService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/provider")
public class RestProviderController extends BaseController {

	private static final int COMMON_CACHE_MAXAGE = 1;

	private final ProviderService providerService;
	private final DepartmentService departmentService;

	@Autowired
	public RestProviderController(DepartmentService departmentService, ProviderService productService) {
		this.providerService = productService;
		this.departmentService = departmentService;
	}

	@GetMapping
	@Secured("ROLE_PERSONNEL")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getAllProviders(@PageableDefault(size = 10) Pageable paging,
			@RequestParam(name = "columns", required = true) List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		departmentService.assertSaleDepartment();

		List<Map<String, Object>> rows = crudService.read(Provider.class, columns, paging,
				ContextProvider.getPrincipalCredential());

		return ResponseEntity.ok(rows);
	}

	@GetMapping("/{providerId}")
	@Secured("ROLE_PERSONNEL")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainProvider(@PathVariable(name = "providerId", required = true) UUID providerId,
			ProviderRequest columnsRequest) throws NoSuchFieldException, UnauthorizedCredential {
		departmentService.assertSaleDepartment();

		Map<String, Object> provider = providerService.find(providerId, columnsRequest,
				ContextProvider.getPrincipalCredential());

		return send(provider, String.format("Provider %s not found", providerId));
	}

	@GetMapping("/count")
	@Secured("ROLE_PERSONNEL")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvidersCount() {
		departmentService.assertSaleDepartment();

		return makeStaleWhileRevalidate(baseRepository.count(Provider.class), COMMON_CACHE_MAXAGE, TimeUnit.DAYS, 3,
				TimeUnit.DAYS);
	}

	@GetMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
	@Transactional(readOnly = true)
	@Secured("ROLE_PERSONNEL")
	public ResponseEntity<?> searchForProviders(ProviderQuery query,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		UUID departmentId = departmentService.getPrincipalDepartment();

		DepartmentScopeContext.assertDepartment(departmentId, DepartmentScopeContext.stock(),
				DepartmentScopeContext.sale());

		if (query.isEmpty()) {
			return sendBadRequest(INVALID_SEARCH_CRITERIA);
		}

		return ResponseEntity
				.ok(providerService.search(columns, paging, query, ContextProvider.getPrincipalCredential()));
	}

}
