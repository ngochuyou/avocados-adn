/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import adn.application.context.ContextProvider;
import adn.application.context.builders.DepartmentScopeContext;
import adn.controller.query.request.ProviderRequest;
import adn.controller.query.specification.ProviderQuery;
import adn.dao.generic.Result;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.Provider;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.services.AuthenticationService;
import adn.service.services.ProviderService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/provider")
public class RestProviderController extends BaseController {

	private static final int COMMON_CACHE_MAXAGE = 1;
	private static final String SUCCESSFUL_APPROVAL = "Successfully approved";

	private final ProviderService providerService;
	private final AuthenticationService authService;

	public RestProviderController(ProviderService providerService, AuthenticationService authService) {
		super();
		this.providerService = providerService;
		this.authService = authService;
	}

	@GetMapping
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getAllProviders(@PageableDefault(size = 10) Pageable paging,
			@RequestParam(name = "columns", required = true) List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		List<Map<String, Object>> rows = crudService.read(Provider.class, columns, paging,
				ContextProvider.getPrincipalCredential());

		return ResponseEntity.ok(rows);
	}

	@GetMapping("/{providerId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainProvider(@PathVariable(name = "providerId", required = true) UUID providerId,
			ProviderRequest columnsRequest) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		Map<String, Object> provider = providerService.find(providerId, columnsRequest,
				ContextProvider.getPrincipalCredential());

		return send(provider, String.format("Provider %s not found", providerId));
	}

	@GetMapping("/count")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvidersCount() {
		authService.assertSaleDepartment();

		return makeStaleWhileRevalidate(baseRepository.count(Provider.class), COMMON_CACHE_MAXAGE, TimeUnit.DAYS, 1,
				TimeUnit.DAYS);
	}

	@GetMapping(path = "/search")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> searchForProviders(ProviderQuery query,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertDepartment(DepartmentScopeContext.stock(), DepartmentScopeContext.sale());

		if (query.isEmpty()) {
			return sendBad(Common.INVALID_SEARCH_CRITERIA);
		}

		return ResponseEntity.ok(providerService.search(columns, paging, query, getPrincipalCredential()));
	}

	@PostMapping
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createProvider(@RequestBody Provider provider) throws UnauthorizedCredential {
		authService.assertSaleDepartment();

		Result<Provider> result = crudService.create(provider.getId(), provider, Provider.class, true);

		return send(result);
	}

	@PutMapping
	@Transactional
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> updateProvider(@RequestBody Provider model) throws UnauthorizedCredential {
		authService.assertSaleDepartment();

		UUID id = model.getId();
		Provider persistence = baseRepository.findById(id, Provider.class);

		if (persistence == null) {
			return sendBad(Common.NOT_FOUND);
		}

		Result<Provider> result = crudService.update(id, model, Provider.class, true);

		return send(result);
	}

	@PatchMapping(path = "/approve/{providerId}")
	@Secured(HEAD)
	@Transactional
	public ResponseEntity<?> approveProvider(@PathVariable(name = "providerId", required = true) UUID providerId) {
		try {
			Result<Provider> result = providerService.approveProvider(providerId, true);

			if (result.isOk()) {
				return ResponseEntity.ok(Result.of(SUCCESSFUL_APPROVAL));
			}

			return sendBad(result.getMessages());
		} catch (Exception e) {
			return fails(e.getMessage());
		}
	}

	@PostMapping(path = "/product-detail")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createProductDetail(@RequestBody ProductProviderDetail model)
			throws UnauthorizedCredential {
		authService.assertSaleDepartment();

		ProductProviderDetail extractedModel = extractorProvider.getExtractor(ProductProviderDetail.class)
				.extract(model);
		Result<ProductProviderDetail> result = providerService.createProductDetail(extractedModel,
				getPrincipalCredential(), true);

		return send(result);
	}

	@PatchMapping(path = "/product-detail/approve")
	@Secured(HEAD)
	@Transactional
	public ResponseEntity<?> approveProductDetail(@RequestParam(name = "providerId", required = true) UUID providerId,
			@RequestParam(name = "productId", required = true) String productId) throws UnauthorizedCredential {
		return send(providerService.approveProductDetail(providerId, productId, true));
	}

}
