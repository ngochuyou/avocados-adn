/**
 * 
 */
package adn.service.services;

import static adn.application.Common.NOT_FOUND;
import static adn.dao.generic.Result.bad;
import static adn.helpers.CollectionHelper.list;
import static adn.service.internal.ServiceResult.of;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import adn.application.Common;
import adn.application.context.ContextProvider;
import adn.controller.query.impl.ProviderQuery;
import adn.dao.generic.Result;
import adn.dao.specific.ProductProviderDetailRepository;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.Provider;
import adn.model.entities.metadata._Provider;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.SourceMetadataFactory;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Service
public class ProviderService {

	private static final String ALREADY_APPROVED = "Resource was already approved";
	private static final String UNAPPROVED_EXIST = "There're other requests waiting to be approved";

	private final ProductProviderDetailRepository productProviderDetailRepository;
	private final AuthenticationService authService;
	private final GenericCRUDServiceImpl crudService;
	private final GenericFactorService genericFactorService;

	public ProviderService(GenericCRUDServiceImpl crudService,
			ProductProviderDetailRepository productProviderDetailRepository, AuthenticationService authService,
			GenericFactorService genericFactorService) {
		this.productProviderDetailRepository = productProviderDetailRepository;
		this.authService = authService;
		this.crudService = crudService;
		this.genericFactorService = genericFactorService;
	}

	public Map<String, Object> findProvider(UUID id, ProviderQuery columnsRequest, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		if (!columnsRequest.hasAssociation()) {
			return crudService.readById(id, Provider.class, columnsRequest.getColumns(), credential);
		}

		List<String> columns = new ArrayList<>(columnsRequest.getColumns());

		columns.remove(_Provider.productDetails);

		Map<String, Object> provider = crudService.readById(id, Provider.class, columns, credential);

		if (provider == null) {
			return null;
		}

		provider.put(_Provider.productDetails, readCurrentProductDetailsByProvider(id,
				list(columnsRequest.getProductDetails().getColumns()), Common.DEFAULT_PAGEABLE, credential));

		return provider;
	}

	public List<Map<String, Object>> searchForProviders(Pageable pageable, ProviderQuery restQuery,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return crudService.readAll(Provider.class, restQuery.getColumns(),
				providerHasId(restQuery).or(GenericFactorService.hasNameLike(restQuery)), pageable, credential);
	}

	public Result<Provider> approveProvider(UUID providerId, boolean flushOnFinish) throws ObjectNotFoundException {
		Session session = ContextProvider.getCurrentSession();
		Provider provider = session.load(Provider.class, providerId, new LockOptions(LockMode.PESSIMISTIC_WRITE));

		if (provider.getApprovedTimestamp() != null) {
			return bad(of(ALREADY_APPROVED));
		}

		provider.setApprovedBy(authService.getHead());
		provider.setApprovedTimestamp(LocalDateTime.now());

		return crudService.finish(session, Result.success(provider), flushOnFinish);
	}

	private static Specification<Provider> providerHasId(ProviderQuery restQuery) {
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

	public List<Map<String, Object>> readAllCurrentProvidersOfProduct(String productId, List<String> columns,
			Pageable paging, Credential credential) throws UnauthorizedCredential, NoSuchFieldException {
		SourceMetadata<Provider> metadata = crudService.optionallyValidate(Provider.class, credential,
				SourceMetadataFactory.unknownArrayCollection(Provider.class, columns));
		List<Object[]> rows = productProviderDetailRepository.findAllCurrentProviderOfProduct(productId,
				metadata.getColumns(), paging);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(Provider.class, rows, credential, metadata);
	}

	/*
	 * ===========================ProductProviderDetail===========================
	 */

	public Result<ProductProviderDetail> createProductDetail(ProductProviderDetail model, Credential credential,
			boolean flushOnFinish) {
		crudService.useManualSession();

		UUID providerId = model.getId().getProviderId();
		String productId = model.getId().getProductId();
		// make sure there's no other details waiting to be approved
		if (productProviderDetailRepository.hasUnapproved(providerId, productId)) {
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
		Optional<ProductProviderDetail> optional = productProviderDetailRepository.findUnapproved(providerId,
				productId);

		if (optional.isEmpty()) {
			return bad(of(NOT_FOUND));
		}

		ProductProviderDetail unapprovedDetail = optional.get();
		Session session = crudService.getCurrentSession();
		// find the current detail
		ProductProviderDetail currentDetail = productProviderDetailRepository.findCurrent(providerId, productId)
				.orElse(null);
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

	public List<Map<String, Object>> readProductDetailsByProduct(String productId, Collection<String> columns,
			Pageable paging, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return genericFactorService.readAll(ProductProviderDetail.class, new Specification<ProductProviderDetail>() {
			@Override
			public Predicate toPredicate(Root<ProductProviderDetail> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				return ProductProviderDetailRepository.hasProductId(root, builder, productId);
			}
		}, columns, paging, credential);
	}

	public List<Map<String, Object>> readCurrentProductDetailsByProvider(UUID providerId, List<String> columns,
			Pageable pageable, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readCurrentProductDetails(providerId, columns, pageable, credential, false);
	}

	public List<Map<String, Object>> readCurrentProductDetailsByProduct(String productId, List<String> columns,
			Pageable pageable, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readCurrentProductDetails(productId, columns, pageable, credential, true);
	}

	private List<Map<String, Object>> readCurrentProductDetails(Serializable identifier, List<String> columns,
			Pageable pageable, Credential credential, boolean byProduct)
			throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<ProductProviderDetail> metadata = crudService.optionallyValidate(ProductProviderDetail.class,
				credential, SourceMetadataFactory.unknownArrayCollection(ProductProviderDetail.class, list(columns)));
		List<Object[]> rows = byProduct
				? productProviderDetailRepository.findAllCurrentByProduct((String) identifier, columns, pageable)
				: productProviderDetailRepository.findAllCurrentByProvider((UUID) identifier, columns, pageable);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(ProductProviderDetail.class, rows, credential, metadata);
	}

}
