/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getCurrentSession;
import static adn.application.context.ContextProvider.getPrincipalCredential;
import static org.springframework.http.ResponseEntity.ok;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import adn.application.context.builders.DepartmentScopeContext;
import adn.controller.query.impl.ProviderQuery;
import adn.dao.specific.ProductCostRepository;
import adn.helpers.CollectionHelper;
import adn.helpers.HibernateHelper;
import adn.model.entities.ProductCost;
import adn.model.entities.Provider;
import adn.model.entities.id.ProductCostId;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;
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

	private static final Logger logger = LoggerFactory.getLogger(RestProviderController.class);

	private static final int COMMON_CACHE_MAXAGE = 1;

	private final ProviderService providerService;
	private final AuthenticationService authService;
	private final ProductCostRepository costRepository;

	public RestProviderController(ProviderService providerService, AuthenticationService authService,
			ProductCostRepository costRepository) {
		super();
		this.providerService = providerService;
		this.authService = authService;
		this.costRepository = costRepository;
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

	@GetMapping(path = "/search")
	@Transactional(readOnly = true)
	@Secured({ HEAD, PERSONNEL })
	public ResponseEntity<?> searchForProviders(ProviderQuery restQuery, @PageableDefault(size = 10) Pageable paging)
			throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertDepartment(DepartmentScopeContext.stock(), DepartmentScopeContext.sale());

		if (!restQuery.hasCriteria()) {
			return bad(ServiceResult.of(Common.INVALID_SEARCH_CRITERIA));
		}

		return makeStaleWhileRevalidate(
				crudService.readAll(Provider.class, CollectionHelper.list(restQuery.getColumns()),
						(root, query, builder) -> builder.like(root.get(_Provider.name), restQuery.getName().getLike()),
						getPrincipalCredential()),
				5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@PostMapping
	@Secured({ HEAD, PERSONNEL })
	@Transactional
	public ResponseEntity<?> createProvider(@RequestBody Provider provider) throws Exception {
		authService.assertSaleDepartment();

		Result<Provider> result = crudService.create(provider.getId(), provider, Provider.class, true);

		return sendAndProduce(result);
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

		return sendAndProduce(result);
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

		return sendAndProduce(providerService.createCost(newCost, getPrincipalCredential(), true));
	}

	@GetMapping(path = "/cost/count")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCostsCount(@RequestParam(name = "product", required = false) BigInteger productId)
			throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		if (productId == null) {
			return makeStaleWhileRevalidate(genericRepository.count(ProductCost.class), 30, TimeUnit.SECONDS, 60,
					TimeUnit.SECONDS);
		}

		return makeStaleWhileRevalidate(
				genericRepository
						.count(ProductCost.class,
								(root, query, builder) -> builder
										.equal(root.get(_ProductCost.id).get(_ProductCost.productId), productId)),
				30, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
	}

//	@GetMapping(path = "/cost")
//	@Secured({ HEAD, PERSONNEL })
//	@Transactional(readOnly = true)
//	public ResponseEntity<?> getCostList(
//			@RequestParam(name = "columns", defaultValue = "") List<String> columns,
//			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
//		authService.assertSaleDepartment();
//
//		List<Map<String, Object>> productDetails = crudService.readAll(ProductCost.class, columns, paging,
//				getPrincipalCredential());
//
//		return ok(productDetails);
//	}

	@GetMapping(path = "/cost")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProductCosts(
			@RequestParam(name = "products", required = false, defaultValue = "") List<BigInteger> productIds,
			@RequestParam(name = "providers", required = false, defaultValue = "") List<UUID> providerIds) {
		authService.assertSaleDepartment();

		if (!productIds.isEmpty()) {
			return findCurrentCostsByProducts(productIds);
		}

//		List<Object[]> costs = findCostsByAssociation(productIds, providerIds, Arrays.asList(_ProductCost.cost)); 
//		
//		return makeStaleWhileRevalidate(costs.stream().collect(HashMap<Object, Object>::new,
//				(map, cols) -> map.put(cols[0], cols[1]), HashMap::putAll), 1, TimeUnit.MINUTES, 5, TimeUnit.MINUTES);
		return ok(new ArrayList<>());
	}

	private ResponseEntity<?> findCurrentCostsByProducts(Collection<BigInteger> productIds) {
		List<Object[]> costs = costRepository.findAllCurrentsByProducts(productIds,
				Arrays.asList(_ProductCost.productId, _ProductCost.cost));

		return makeStaleWhileRevalidate(costs.stream().collect(HashMap<Object, Object>::new,
				(map, cols) -> map.put(cols[0], cols[1]), HashMap::putAll), 1, TimeUnit.MINUTES, 5, TimeUnit.MINUTES);
	}

	@GetMapping(path = "/cost/{productId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCostsByProduct(@PathVariable(name = "productId", required = true) String productId,
			@PageableDefault(size = 10) Pageable paging,
			@RequestParam(name = "columns", required = false, defaultValue = "") List<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		return makeStaleWhileRevalidate(
				crudService.readAll(ProductCost.class, columns,
						(root, query, builder) -> builder.equal(root.get(_ProductCost.id).get(_ProductCost.productId),
								productId),
						paging, getPrincipalCredential()),
				5, TimeUnit.SECONDS, 7, TimeUnit.SECONDS);
	}

	@GetMapping(path = "/cost/providersbyproduct/{productId}")
	@Secured({ HEAD, PERSONNEL })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvidersByProduct(@PathVariable(name = "productId", required = true) String productId,
			@PageableDefault(size = 10) Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		authService.assertSaleDepartment();

		Session session = getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<ProductCost> root = cq.from(ProductCost.class);
		Join<Object, Object> providerJoin = root.join(_ProductCost.provider);

		cq.distinct(true).multiselect(providerJoin.get(_Provider.id), providerJoin.get(_Provider.name)).where(
				builder.and(builder.equal(root.get(_ProductCost.id).get(_ProductCost.productId), productId)),
				builder.equal(root.get(_ProductCost.active), true));

		Query<Tuple> hql = session.createQuery(cq);

		hql.setMaxResults(paging.getPageSize());
		hql.setFirstResult(paging.getPageNumber() * paging.getPageSize());
		
		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return makeStaleWhileRevalidate(HibernateHelper.toRows(hql.list()).stream()
				.map(cols -> Map.of("id", cols[0], "name", cols[1])).collect(Collectors.toList()), 30, TimeUnit.DAYS, 7,
				TimeUnit.DAYS);
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

		return sendAndProduce(genericService.approve(ProductCost.class, persistence.get().getId(), true));
	}

}
