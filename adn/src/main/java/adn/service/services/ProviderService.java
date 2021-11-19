/**
 * 
 */
package adn.service.services;

import static adn.application.Result.bad;
import static adn.helpers.CollectionHelper.list;
import static adn.helpers.HibernateHelper.useManualSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import adn.application.Common;
import adn.application.Result;
import adn.controller.query.impl.ProviderQuery;
import adn.dao.specific.ProductCostRepository;
import adn.helpers.CollectionHelper;
import adn.model.entities.ProductCost;
import adn.model.entities.Provider;
import adn.model.entities.id.ProductCostId;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.SourceMetadataFactory;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class ProviderService {

	private final ProductCostRepository productCostRepository;
	private final GenericCRUDServiceImpl crudService;

	private static final String OVERLAPPED_COST_TEMPLATE = "%s has already exsited";

	@Autowired
	public ProviderService(GenericCRUDServiceImpl crudService, ProductCostRepository productProviderDetailRepository) {
		this.productCostRepository = productProviderDetailRepository;
		this.crudService = crudService;
	}

	public Map<String, Object> findProvider(UUID id, ProviderQuery columnsRequest, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		if (!columnsRequest.hasAssociation()) {
			return crudService.readById(id, Provider.class, columnsRequest.getColumns(), credential);
		}

		List<String> columns = new ArrayList<>(columnsRequest.getColumns());

		columns.remove(_Provider.productCosts);

		Map<String, Object> provider = crudService.readById(id, Provider.class, columns, credential);

		if (provider == null) {
			return null;
		}

		provider.put(_Provider.productCosts, readCostsByProvider(id,
				list(columnsRequest.getProductDetails().getColumns()), Common.DEFAULT_PAGEABLE, credential));

		return provider;
	}

//	public List<Map<String, Object>> searchForProviders(Pageable pageable, ProviderQuery restQuery,
//			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
//		return crudService.readAll(Provider.class, restQuery.getColumns(),
//				providerHasId(restQuery).or(GenericFactorService.hasNameLike(restQuery)), pageable, credential);
//	}

//	private static Specification<Provider> providerHasId(ProviderQuery restQuery) {
//		return new Specification<Provider>() {
//			@Override
//			public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
//				if (restQuery.getId() == null || restQuery.getId().getEquals() == null) {
//					return null;
//				}
//
//				return builder.equal(root.get(_Provider.id), restQuery.getId().getEquals());
//			}
//		};
//	}

	public List<Map<String, Object>> readAllCurrentProvidersOfProduct(String productId, List<String> columns,
			Pageable paging, Credential credential) throws UnauthorizedCredential, NoSuchFieldException {
		SourceMetadata<Provider> metadata = crudService.optionallyValidate(Provider.class, credential,
				SourceMetadataFactory.unknownArrayCollection(Provider.class, columns));
		List<Object[]> rows = productCostRepository.findAllCurrentProvidersOfProduct(productId, metadata.getColumns(),
				paging);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(Provider.class, rows, credential, metadata);
	}

	/*
	 * ===========================ProductProviderDetail===========================
	 */

	public Result<ProductCost> createCost(ProductCost newCost, Credential credential, boolean flushOnFinish) {
		useManualSession();

		ProductCostId newCostId = newCost.getId();
		ProductCostId overlappedCostId = productCostRepository
				.findOverlapping(newCostId.getProviderId(), newCostId.getProductId(), newCostId.getAppliedTimestamp(),
						newCostId.getDroppedTimestamp(), Arrays.asList(_ProductCost.id))
				.map(cols -> (ProductCostId) cols[0]).orElse(null);
		// check for overlapping
		if (overlappedCostId != null) {
			return bad(String.format(OVERLAPPED_COST_TEMPLATE, overlappedCostId));
		}
		// then we insert the new cost
		return crudService.create(newCostId, newCost, ProductCost.class, flushOnFinish);
	}

//	public List<Map<String, Object>> readCostsByProduct(String productId, Collection<String> columns,
//			Pageable paging, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
//		return crudService.readAll(ProductCost.class, columns,
//				(root, query, builder) -> ProductCostRepository.hasId(root, builder, productId), paging, credential);
//	}
//
	public List<Map<String, Object>> readCostsByProvider(UUID providerId, List<String> columns, Pageable pageable,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readCurrentProductDetails(providerId, columns, pageable, credential, false);
	}

//
//	public List<Map<String, Object>> readCurrentCostsByProduct(String productId, List<String> columns,
//			Pageable pageable, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
//		return readCurrentProductDetails(productId, columns, pageable, credential, true);
//	}
//
	private List<Map<String, Object>> readCurrentProductDetails(Serializable identifier, List<String> columns,
			Pageable pageable, Credential credential, boolean byProduct)
			throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<ProductCost> metadata = crudService.optionallyValidate(ProductCost.class, credential,
				SourceMetadataFactory.unknownArrayCollection(ProductCost.class, list(columns)));
		List<Object[]> rows = byProduct
				? productCostRepository.findAllCurrentByProduct((String) identifier, columns, pageable)
				: productCostRepository.findAllCurrentByProvider((UUID) identifier, columns, pageable);

		if (CollectionHelper.isEmpty(rows)) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(ProductCost.class, rows, credential, metadata);
	}

}
