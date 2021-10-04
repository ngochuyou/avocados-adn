/**
 * 
 */
package adn.service.services;

import static adn.dao.generic.Result.bad;
import static adn.helpers.HibernateHelper.useManualSession;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Common;
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.Result;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.Category;
import adn.model.entities.Product;
import adn.model.entities.metadata._Product;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.internal.ResourceService;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Service
public class ProductService implements Service {

	private final ResourceService resourceService;
	private final GenericCRUDServiceImpl crudService;
	private final AuthenticationService authenticationService;

	private static final String MAXIMUM_IMAGES_AMOUNT_EXCEEDED = String.format("Cannot upload more than %d files",
			_Product.MAXIMUM_IMAGES_AMOUNT);

	public ProductService(ResourceService resourceService, GenericCRUDServiceImpl curdService,
			AuthenticationService authenticationService) {
		super();
		this.resourceService = resourceService;
		this.crudService = curdService;
		this.authenticationService = authenticationService;
	}

	public List<Map<String, Object>> readProductsByCategory(Long categoryIdentifier, String categoryIdentifierProperty,
			Collection<String> requestedColumns, Pageable paging, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		// @formatter:off
		return crudService.readAllByAssociation(
				Product.class,
				Category.class,
				_Product.category,
				categoryIdentifierProperty,
				categoryIdentifier,
				requestedColumns,
				paging,
				credential,
				null);
		// @formatter:on
	}

	public Result<Product> createProduct(Product product, MultipartFile[] images, boolean flushOnFinish) {
		Session session = crudService.getCurrentSession();

		useManualSession(session);

		boolean isResourceSessionFlushed = false;

		if (images != null && images.length != 0) {
			if (images.length > _Product.MAXIMUM_IMAGES_AMOUNT) {
				return bad(Map.of(_Product.images, MAXIMUM_IMAGES_AMOUNT_EXCEEDED));
			}

			ServiceResult<String[]> uploadResult = resourceService.uploadProductImages(images);

			if (!uploadResult.isOk()) {
				return bad(Map.of(_Product.images, Common.UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			product.setImages(Stream.of(uploadResult.getBody()).collect(Collectors.toList()));
		}

		Result<Product> result = crudService.create(null, product, Product.class, false);

		resourceService.closeSession(result.isOk() && isResourceSessionFlushed && flushOnFinish);

		return crudService.finish(session, result, flushOnFinish);
	}

	public Result<Product> updateProduct(Product model, MultipartFile[] savedImages, boolean flushOnFinish) {
		Session session = crudService.getCurrentSession();

		useManualSession(session);

		Product persistence = session.load(Product.class, model.getId());

		boolean isResourceSessionFlushed = false;
		List<String> newImagesState = model.getImages();
		List<String> removedImages = persistence.getImages().stream()
				.filter(filename -> !newImagesState.contains(filename)).collect(Collectors.toList());

		if (removedImages.size() != 0) {
			ServiceResult<String> removeResult = resourceService.removeProductImages(removedImages);

			if (!removeResult.isOk()) {
				return bad(Map.of(_Product.images, removeResult.getBody()));
			}

			isResourceSessionFlushed = true;
		}

		if (savedImages.length != 0) {
			if (newImagesState.size() + savedImages.length > _Product.MAXIMUM_IMAGES_AMOUNT) {
				return bad(Map.of(_Product.images, MAXIMUM_IMAGES_AMOUNT_EXCEEDED));
			}

			ServiceResult<String[]> uploadResult = resourceService.uploadProductImages(savedImages);

			if (!uploadResult.isOk()) {
				return bad(Map.of(_Product.images, Common.UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			newImagesState.addAll(Set.of(uploadResult.getBody()));
		}

		model.setImages(newImagesState);

		Result<Product> result = crudService.update(model.getId(), model, Product.class, false);

		resourceService.closeSession(isResourceSessionFlushed && result.isOk() && flushOnFinish);

		return crudService.finish(session, result, flushOnFinish);
	}

	public Result<Product> approveProduct(BigInteger productId, boolean flushOnFinish) {
		Session session = crudService.getCurrentSession();

		useManualSession(session);

		Product product = session.load(Product.class, productId, LockMode.PESSIMISTIC_WRITE);

		if (product.getApprovedTimestamp() != null) {
			return crudService
					.finish(Result.bad(Map.of(Common.MESSAGE, String.format("Product was already approved on %s",
							Utils.localDateTime(product.getApprovedTimestamp())))), flushOnFinish);
		}

		product.setApprovedBy(authenticationService.getHead());
		product.setApprovedTimestamp(LocalDateTime.now());
		session.save(product);

		return crudService.finish(Result.success(product), flushOnFinish);
	}

	public List<Map<String, Object>> searchProduct(Collection<String> requestedColumns, Pageable pageable,
			ProductQuery restQuery, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return crudService.readAll(Product.class, requestedColumns,
				SpecificationUtils.hasNameLike(restQuery).or(hasIdLike(restQuery)), pageable, credential);
	}

//	@Override
//	public Map<String, Object> findActiveById(Serializable id, Class<Product> type, Collection<String> requestedColumns,
//			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
//		boolean stockDetailsRequired = false;
//		Set<String> columns = new HashSet<>(requestedColumns);
//
//		if (stockDetailsRequired = requestedColumns.contains(_Product.stockDetails)) {
//			columns.remove(_Product.stockDetails);
//		}
//
//		Map<String, Object> product = super.findOneActive(id, type, columns, credential);
//
//		if (!stockDetailsRequired || product == null) {
//			return product;
//		}
//
//		List<Map<String, Object>> stockDetails = stockDetailService.readActiveOnly(id, Collections.emptyList(),
//				credential);
//
//		product.put(_Product.stockDetails, stockDetails);
//
//		return product;
//	}

	private static Specification<Product> hasIdLike(ProductQuery restQuery) {
		return new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getId() == null || !StringHelper.hasLength(restQuery.getId().getLike())) {
					return null;
				}

				return builder.like(root.get("id"), restQuery.getId().getLike());
			}
		};
	}

}
