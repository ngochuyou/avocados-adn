/**
 * 
 */
package adn.service.services;

import static adn.model.entities.ProductProviderDetail.DROPPED_TIMESTAMP_FIELD;
import static adn.model.entities.ProductProviderDetail.ID_APPLIED_TIMESTAMP_FIELD;
import static adn.model.entities.ProductProviderDetail.ID_FIELD;
import static adn.model.entities.Provider.PRODUCT_DETAILS_FIELD;

import java.io.Serializable;
import java.util.Arrays;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import adn.controller.query.request.ProviderRequest;
import adn.controller.query.specification.ProviderQuery;
import adn.dao.generic.Repository;
import adn.dao.specification.GenericFactorRepository;
import adn.dao.specification.Selections;
import adn.helpers.StringHelper;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Service
public class ProviderService extends AbstractFactorService<Provider> {

	private static final Collection<String> FETCHED_PRODUCT_DETAILS_COLUMNS = Arrays.asList(ID_FIELD,
			ProductProviderDetail.PRICE_FIELD, DROPPED_TIMESTAMP_FIELD);
	private static final Map<String, String> PRODUCT_DETAIL_COLUMN_PATH_MAP;
	private static final Map<String, BiFunction<String, Root<ProductProviderDetail>, Selection<?>>> PRODUCT_DETAIL_COLUMN_PATH_RESOLVERS;

	static {
		Map<String, String> pathMap = new HashMap<>(0);
		String productIdPath = ProductProviderDetail.ID_PRODUCT_FIELD;
		String providerIdPath = ProductProviderDetail.ID_PROVIDER_FIELD;
		String appliedTimestampPath = ProductProviderDetail.ID_APPLIED_TIMESTAMP_FIELD;

		pathMap.put(productIdPath, productIdPath);
		pathMap.put(providerIdPath, providerIdPath);
		pathMap.put(appliedTimestampPath, appliedTimestampPath);

		PRODUCT_DETAIL_COLUMN_PATH_MAP = Collections.unmodifiableMap(pathMap);

		Map<String, BiFunction<String, Root<ProductProviderDetail>, Selection<?>>> pathResolvers = new HashMap<>(0);

		pathResolvers.put(productIdPath,
				(columnName, root) -> root.get(ProductProviderDetail.ID_FIELD).get(productIdPath));
		pathResolvers.put(providerIdPath,
				(columnName, root) -> root.get(ProductProviderDetail.ID_FIELD).get(providerIdPath));
		pathResolvers.put(appliedTimestampPath,
				(columnName, root) -> root.get(ProductProviderDetail.ID_FIELD).get(appliedTimestampPath));
		pathResolvers.put(null, (columnName, root) -> root.get(columnName));

		PRODUCT_DETAIL_COLUMN_PATH_RESOLVERS = Collections.unmodifiableMap(pathResolvers);
	}

	@Autowired
	public ProviderService(GenericCRUDService crudService, Repository repository,
			GenericFactorRepository factorRepository) {
		super(crudService, repository, factorRepository);
	}

	private Selections<ProductProviderDetail> resolveProductDetailColumnPaths(Collection<String> columnNames) {
		return new Selections<ProductProviderDetail>() {
			@Override
			public List<Selection<?>> toSelections(Root<ProductProviderDetail> root) {
				return columnNames.stream()
						.map(column -> PRODUCT_DETAIL_COLUMN_PATH_RESOLVERS
								.get(PRODUCT_DETAIL_COLUMN_PATH_MAP.get(column))
								.apply(column, (Root<ProductProviderDetail>) root))
						.collect(Collectors.toList());
			}
		};
	}

	public Map<String, Object> find(Serializable id, ProviderRequest columnsRequest, UUID principalDepartment)
			throws NoSuchFieldException {
		boolean isProductDetailsCollectivelyRequested = columnsRequest.isProductDetailsCollectivelyRequested();
		boolean isProductDetailsSpecificallyRequested = columnsRequest.isProductDetailsSpecificallyRequested();

		if (!isProductDetailsCollectivelyRequested && !isProductDetailsSpecificallyRequested) {
			return crudService.find(id, Provider.class, columnsRequest.getColumns(), principalDepartment);
		}

		List<String> columns = columnsRequest.getColumns();

		if (isProductDetailsCollectivelyRequested) {
			columns.remove(PRODUCT_DETAILS_FIELD);
		}

		Map<String, Object> fetchedProviderColumns = crudService.find(id, Provider.class, columns);
		List<Map<String, Object>> fetchedProductDetailsColumns = findProductDetailsByProviderDetails(id,
				isProductDetailsCollectivelyRequested ? FETCHED_PRODUCT_DETAILS_COLUMNS
						: columnsRequest.getProductDetailsColumns(),
				principalDepartment, isProductDetailsSpecificallyRequested);

		fetchedProviderColumns.put(PRODUCT_DETAILS_FIELD, fetchedProductDetailsColumns);

		return fetchedProviderColumns;
	}

	public List<Map<String, Object>> findProductDetailsByProviderDetails(Serializable providerId,
			Collection<String> requestedColumns, UUID principalDepartment, boolean isPathResolvingNeeded)
			throws NoSuchFieldException {
		return crudService.read(ProductProviderDetail.class, requestedColumns,
				new Specification<ProductProviderDetail>() {

					@Override
					public Predicate toPredicate(Root<ProductProviderDetail> root, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						return builder.and(
								builder.equal(root.get(ID_FIELD).get(ProductProviderDetail.ID_PROVIDER_FIELD),
										providerId),
								builder.isNotNull(root.get(ID_FIELD).get(ID_APPLIED_TIMESTAMP_FIELD)),
								builder.isNull(root.get(DROPPED_TIMESTAMP_FIELD)));
					}

				}, principalDepartment, isPathResolvingNeeded ? this::resolveProductDetailColumnPaths : null);
	}

	public List<Map<String, Object>> search(Collection<String> requestedColumns, Pageable pageable,
			ProviderQuery restQuery, UUID departmentId) throws NoSuchFieldException {
		return crudService.read(Provider.class, requestedColumns, hasId(restQuery).or(hasNameLike(restQuery)), pageable,
				departmentId);
	}

	private static Specification<Provider> hasId(ProviderQuery restQuery) {
		return new Specification<Provider>() {
			@Override
			public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getId() == null || restQuery.getId().getEquals() == null) {
					return null;
				}

				return builder.equal(root.get("id"), restQuery.getId().getEquals());
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

				return builder.like(root.get("name"), restQuery.getName().getLike());
			}
		};
	}

}
