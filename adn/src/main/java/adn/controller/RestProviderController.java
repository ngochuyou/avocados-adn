/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static org.springframework.http.ResponseEntity.ok;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import adn.application.context.ContextProvider;
import adn.controller.query.impl.ProviderQuery;
import adn.model.entities.ProductCost;
import adn.model.entities.Provider;
import adn.model.entities.id.ProductCostId;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.ProductCostModel;
import adn.service.internal.ServiceResult;
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
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		List<Map<String, Object>> rows = crudService.readAll(Provider.class, columns, paging,
				ContextProvider.getPrincipalCredential());

		return ok(rows);
	}

	@GetMapping("/{providerId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainProvider(@PathVariable(name = "providerId", required = true) UUID providerId,
			ProviderQuery query) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		Map<String, Object> provider = providerService.findProvider(providerId, query,
				ContextProvider.getPrincipalCredential());

		return send(provider, ServiceResult.of(String.format("Provider %s not found", providerId)));
	}

	@GetMapping("/count")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvidersCount() {
		authService.assertSaleDepartment();

		return makeStaleWhileRevalidate(genericRepository.count(Provider.class), COMMON_CACHE_MAXAGE, TimeUnit.DAYS, 1,
				TimeUnit.DAYS);
	}

//	@GetMapping(path = "/search")
//	@Transactional(readOnly = true)
//	@Secured({ HEAD, PERSONNEL })
//	public ResponseEntity<?> searchForProviders(ProviderQuery query, @PageableDefault(size = 10) Pageable paging)
//			throws NoSuchFieldException, UnauthorizedCredential {
//		authService.assertDepartment(DepartmentScopeContext.stock(), DepartmentScopeContext.sale());
//
//		if (!query.hasCriteria()) {
//			return sendBad(ServiceResult.of(Common.INVALID_SEARCH_CRITERIA));
//		}
//
//		return ResponseEntity.ok(providerService.searchForProviders(paging, query, getPrincipalCredential()));
//	}

	@PostMapping
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createProvider(@RequestBody Provider provider) throws Exception {
		authService.assertSaleDepartment();

		Result<Provider> result = crudService.create(provider.getId(), provider, Provider.class, true);

		return send(result);
	}

	@PutMapping
	@Transactional
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> updateProvider(@RequestBody Provider model) throws Exception {
		authService.assertSaleDepartment();

		UUID id = model.getId();
		Optional<Provider> optional = genericRepository.findById(Provider.class, id);

		if (optional.isEmpty()) {
			return notFound();
		}

		Result<Provider> result = crudService.update(id, model, Provider.class, true);

		return send(result);
	}

	@GetMapping(path = "/current/{productId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCurrentProviderOfProduct(
			@PathVariable(name = "productId", required = true) String productId,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		List<Map<String, Object>> providers = providerService.readAllCurrentProvidersOfProduct(productId, columns,
				paging, getPrincipalCredential());

		return makeStaleWhileRevalidate(providers, 60, TimeUnit.SECONDS, 120, TimeUnit.SECONDS);
	}

	/*
	 * ===========================ProductCost===========================
	 */

	@PostMapping(path = "/cost")
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createProductCost(@RequestBody ProductCostModel model) throws Exception {
		authService.assertSaleDepartment();

		ProductCost newCost = extract(ProductCost.class, model, new ProductCost());

		return send(providerService.createCost(newCost, getPrincipalCredential(), true));
	}

	@GetMapping(path = "/cost/count")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCostsCount() throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		return makeStaleWhileRevalidate(genericRepository.count(ProductCost.class), 30, TimeUnit.SECONDS, 60,
				TimeUnit.SECONDS);
	}

//	@GetMapping(path = "/cost/{productId}")
//	@Secured({ HEAD, PERSONNEL })
//	@Transactional(readOnly = true)
//	public ResponseEntity<?> getCostsByProduct(
//			@PathVariable(name = "productId", required = true) String productId,
//			@PageableDefault(size = 10) Pageable paging,
//			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
//			throws NoSuchFieldException, UnauthorizedCredential {
//		authService.assertSaleDepartment();
//
//		List<Map<String, Object>> productDetails = providerService.readCostsByProduct(productId, columns,
//				paging, getPrincipalCredential());
//
//		return makeStaleWhileRevalidate(productDetails, 90, TimeUnit.SECONDS, 5, TimeUnit.MINUTES);
//	}

	@GetMapping(path = "/cost")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCostList(
			@RequestParam(name = "columns", defaultValue = "") List<String> columns,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		List<Map<String, Object>> productDetails = crudService.readAll(ProductCost.class, columns, paging,
				getPrincipalCredential());

		return ok(productDetails);
	}

	@PatchMapping(path = "/cost/approve")
	@Secured(HEAD)
	@Transactional
	public ResponseEntity<?> approveCost(@RequestParam(name = "provider") UUID providerId,
			@RequestParam(name = "product") BigInteger productId,
			@RequestParam(name = "applied") @DateTimeFormat(pattern = Common.COMMON_LDT_FORMAT) LocalDateTime appliedTimestamp,
			@RequestParam(name = "dropped") @DateTimeFormat(pattern = Common.COMMON_LDT_FORMAT) LocalDateTime droppedTimestamp)
			throws Exception {
		ProductCostId costId = new ProductCostId(productId, providerId, appliedTimestamp, droppedTimestamp);
		Optional<ProductCost> persistence = genericRepository.findById(ProductCost.class, costId);

		if (persistence.isEmpty()) {
			return notFound(Common.message(Common.notfound(costId)));
		}

		return send(genericService.approve(ProductCost.class, persistence.get().getId(), true));
	}

}
