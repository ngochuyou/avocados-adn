/**
 * 
 */
package adn.service.services;

import static adn.dao.generic.Result.bad;
import static adn.service.services.GenericPermanentEntityService.isActive;

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

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Common;
import adn.application.context.internal.EffectivelyFinal;
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
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
public class ProductService implements Service, EffectivelyFinal {

	private final ResourceService resourceService;
	private final GenericCRUDServiceImpl crudService;

	public static final int MAXIMUM_IMAGES_AMOUNT = 20;
	protected static String PRODUCT_ENTITY_NAME;

	public ProductService(ResourceService resourceService, GenericCRUDServiceImpl curdService) {
		super();
		this.resourceService = resourceService;
		this.crudService = curdService;
	}

	public List<Map<String, Object>> getProductsByCategory(String categoryIdentifier, String categoryIdentifierProperty,
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
				isActive(Product.class));
		// @formatter:on
	}

	public Result<Product> createProduct(Product product, MultipartFile[] images, boolean flushOnFinish) {
		Session session = crudService.getCurrentSession();

		session.setHibernateFlushMode(FlushMode.MANUAL);

		boolean isResourceSessionFlushed = false;

		if (images != null && images.length != 0) {
			if (images.length > MAXIMUM_IMAGES_AMOUNT) {
				return bad(Map.of("images", String.format("Cannot upload more than %d files", MAXIMUM_IMAGES_AMOUNT)));
			}

			ServiceResult<String[]> uploadResult = resourceService.uploadProductImages(images);

			if (!uploadResult.isOk()) {
				return bad(Map.of("images", Common.UPLOAD_FAILURE));
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

		session.setHibernateFlushMode(FlushMode.MANUAL);

		Product persistence = session.load(Product.class, model.getId());

		boolean isResourceSessionFlushed = false;
		List<String> newImagesState = model.getImages();
		List<String> removedImages = persistence.getImages().stream()
				.filter(filename -> !newImagesState.contains(filename)).collect(Collectors.toList());

		if (removedImages.size() != 0) {
			ServiceResult<String> removeResult = resourceService.removeProductImages(removedImages);

			if (!removeResult.isOk()) {
				return bad(Map.of("images", removeResult.getBody()));
			}

			isResourceSessionFlushed = true;
		}

		if (savedImages.length != 0) {
			if (newImagesState.size() + savedImages.length > MAXIMUM_IMAGES_AMOUNT) {
				return bad(Map.of("images", String.format("Cannot upload more than %d files", MAXIMUM_IMAGES_AMOUNT)));
			}

			ServiceResult<String[]> uploadResult = resourceService.uploadProductImages(savedImages);

			if (!uploadResult.isOk()) {
				return bad(Map.of("images", Common.UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			newImagesState.addAll(Set.of(uploadResult.getBody()));
		}

		model.setImages(newImagesState);

		Result<Product> result = crudService.update(model.getId(), model, Product.class, false);

		resourceService.closeSession(isResourceSessionFlushed && result.isOk() && flushOnFinish);

		return crudService.finish(session, result, flushOnFinish);
	}

	public List<Map<String, Object>> searchProduct(Collection<String> requestedColumns, Pageable pageable,
			ProductQuery restQuery, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return crudService.readAll(Product.class, requestedColumns,
				isActive(Product.class).and(GenericFactorService.hasNameLike(restQuery).or(hasIdLike(restQuery))),
				pageable, credential);
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

	private Access access = new Access() {
		@Override
		public void close() {
			access = null;
		}

		public void execute() throws Exception {
			PRODUCT_ENTITY_NAME = HibernateHelper.getEntityName(Product.class);
		};
	};

	@Override
	public Access getAccess() throws IllegalAccessException {
		return access;
	}

}
