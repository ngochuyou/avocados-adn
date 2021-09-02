/**
 * 
 */
package adn.service.services;

import static adn.dao.generic.Result.bad;
import static adn.dao.generic.Result.of;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import adn.application.Common;
import adn.application.context.ContextProvider;
import adn.controller.query.request.ProviderRequest;
import adn.controller.query.specification.ProviderQuery;
import adn.dao.generic.Repository;
import adn.dao.generic.Result;
import adn.dao.specification.GenericFactorRepository;
import adn.dao.specification.ProductProviderDetailRepository;
import adn.dao.specification.Selections;
import adn.helpers.StringHelper;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.Provider;
import adn.model.entities.metadata._ProductProviderDetail;
import adn.model.entities.metadata._Provider;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Service
public class ProviderService extends AbstractFactorService<Provider> {

	private static final Map<String, String> PRODUCT_DETAIL_COLUMN_PATH_MAP;
	private static final Map<String, BiFunction<String, Root<ProductProviderDetail>, Selection<?>>> PRODUCT_DETAIL_COLUMN_PATH_RESOLVERS;

	private static final String ALREADY_APPROVED = "Resource was already approved";
	private static final String UNAPPROVED_EXIST = "There're other requests waiting to be approved";

	static {
		Map<String, String> pathMap = new HashMap<>(0);
		String productIdPath = _ProductProviderDetail.productId;
		String providerIdPath = _ProductProviderDetail.providerId;
		String createdTimestampPath = _ProductProviderDetail.createdTimestamp;

		pathMap.put(productIdPath, productIdPath);
		pathMap.put(providerIdPath, providerIdPath);
		pathMap.put(createdTimestampPath, createdTimestampPath);

		PRODUCT_DETAIL_COLUMN_PATH_MAP = Collections.unmodifiableMap(pathMap);

		Map<String, BiFunction<String, Root<ProductProviderDetail>, Selection<?>>> pathResolvers = new HashMap<>(0);
		// @formatter:off
		pathResolvers.put(productIdPath,
				(columnName, root) -> root.get(_ProductProviderDetail.id).get(productIdPath));
		pathResolvers.put(providerIdPath,
				(columnName, root) -> root.get(_ProductProviderDetail.id).get(providerIdPath));
		pathResolvers.put(createdTimestampPath,
				(columnName, root) -> root.get(_ProductProviderDetail.id).get(createdTimestampPath));
		pathResolvers.put(null, (columnName, root) -> root.get(columnName));
		// @formatter:on

		PRODUCT_DETAIL_COLUMN_PATH_RESOLVERS = Collections.unmodifiableMap(pathResolvers);
	}

	private final ProductProviderDetailRepository productProviderDetailRepository;
	private final AuthenticationService authService;

	@Autowired
	public ProviderService(GenericCRUDService crudService, Repository repository,
			GenericFactorRepository factorRepository, ProductProviderDetailRepository productProviderDetailRepository,
			AuthenticationService authService) {
		super(crudService, repository, factorRepository);
		this.productProviderDetailRepository = productProviderDetailRepository;
		this.authService = authService;
	}

	private Selections<ProductProviderDetail> resolveProductDetailColumnPaths(Collection<String> columnNames) {
		return new Selections<ProductProviderDetail>() {
			@Override
			public List<Selection<?>> toSelections(Root<ProductProviderDetail> root) {
				return columnNames.stream()
						.map(column -> PRODUCT_DETAIL_COLUMN_PATH_RESOLVERS
								.get(PRODUCT_DETAIL_COLUMN_PATH_MAP.get(column)).apply(column, root))
						.collect(Collectors.toList());
			}
		};
	}

	public Map<String, Object> find(UUID id, ProviderRequest columnsRequest, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		if (!columnsRequest.hasAssociation()) {
			return crudService.find(id, Provider.class, columnsRequest.getColumns(), credential);
		}

		List<String> columns = columnsRequest.getColumns();

		columns.remove(_Provider.productDetails);

		Map<String, Object> fetchedProviderColumns = crudService.find(id, Provider.class, columns, credential);
		List<Map<String, Object>> fetchedProductDetailsColumns = findCurrentProductDetailsByProvider(id,
				columnsRequest.getProductDetails().getColumns(), credential);

		fetchedProviderColumns.put(_Provider.productDetails, fetchedProductDetailsColumns);

		return fetchedProviderColumns;
	}

	private List<Map<String, Object>> findCurrentProductDetailsByProvider(UUID providerId,
			Collection<String> requestedColumns, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		return crudService.read(ProductProviderDetail.class, requestedColumns,
				new Specification<ProductProviderDetail>() {
				// @formatter:off
					@Override
					public Predicate toPredicate(Root<ProductProviderDetail> root, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
				
						return builder.and(
							builder.equal(root.get(_ProductProviderDetail.id).get(_ProductProviderDetail.providerId), providerId),
							ProductProviderDetailRepository.isCurrent(root, builder)
						);
					// @formatter:on
					}

				}, credential, this::resolveProductDetailColumnPaths);
	}

	public List<Map<String, Object>> search(Collection<String> requestedColumns, Pageable pageable,
			ProviderQuery restQuery, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return crudService.read(Provider.class, requestedColumns, hasId(restQuery).or(hasNameLike(restQuery)), pageable,
				credential);
	}

	public Result<ProductProviderDetail> createProductDetail(ProductProviderDetail model, Credential credential,
			boolean flushOnFinish) {
		crudService.useManualSession();

		UUID providerId = model.getId().getProviderId();
		String productId = model.getId().getProductId();
		// make sure there's no other details waiting to be approved
		if (productProviderDetailRepository.hasUnapprovedDetail(providerId, productId)) {
			return bad(of(UNAPPROVED_EXIST));
		}
		// then we insert the new detail
		Result<ProductProviderDetail> insertResult = crudService.create(model.getId(), model,
				ProductProviderDetail.class, false);

		if (!insertResult.isOk()) {
			return insertResult;
		}

		return crudService.finish(insertResult, flushOnFinish);
	}

	public Result<ProductProviderDetail> approveProductDetail(UUID providerId, String productId,
			boolean flushOnFinish) {
		crudService.useManualSession();
		// make sure there's an "unapproved"
		ProductProviderDetail unapprovedDetail = productProviderDetailRepository.findUnapprovedProductDetail(providerId,
				productId);

		if (unapprovedDetail == null) {
			return bad(of(Common.NOT_FOUND));
		}

		Session session = crudService.getCurrentSession();
		// find the current detail
		ProductProviderDetail currentDetail = productProviderDetailRepository.findCurrentProductDetail(providerId,
				productId);
		LocalDateTime timestamp = LocalDateTime.now();

		if (currentDetail != null) {
			// drop it
			session.lock(currentDetail, LockMode.PESSIMISTIC_WRITE);
			currentDetail.setDroppedTimestamp(timestamp);
		}
		// lock the unapproved detail
		session.lock(unapprovedDetail, LockMode.PESSIMISTIC_WRITE);

		unapprovedDetail.setApprovedBy(authService.getHead());
		unapprovedDetail.setApprovedTimestamp(timestamp);

		return crudService.finish(Result.success(unapprovedDetail), flushOnFinish);
	}

	public Result<Provider> approveProvider(UUID providerId, boolean flushOnFinish) throws ObjectNotFoundException {
		Session session = ContextProvider.getCurrentSession();
		Provider provider = session.load(Provider.class, providerId, new LockOptions(LockMode.PESSIMISTIC_WRITE));

		if (provider.getApprovedTimestamp() != null) {
			return Result.bad(Result.of(ALREADY_APPROVED));
		}

		provider.setApprovedBy(authService.getHead());
		provider.setApprovedTimestamp(LocalDateTime.now());

		return crudService.finish(session, Result.success(provider), flushOnFinish);
	}

	private static Specification<Provider> hasId(ProviderQuery restQuery) {
		return new Specification<Provider>() {
			@Override
			public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getId() == null || restQuery.getId().getEquals() == null) {
					return null;
				}

				return builder.equal(root.get(_Provider.id), restQuery.getId().getEquals());
			}
		};
	}

	private static Specification<Provider> hasNameLike(ProviderQuery restQuery) {
		return new Specification<Provider>() {
			@Override
			public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getName() == null || !StringHelper.hasLength(restQuery.getName().getLike())) {
					return null;
				}

				return builder.like(root.get(_Provider.name), restQuery.getName().getLike());
			}
		};
	}

}
