/**
 * 
 */
package adn.service.services;

import static adn.dao.generic.Result.bad;
import static adn.helpers.CollectionHelper.from;
import static adn.helpers.HibernateHelper.toRows;
import static adn.model.entities.Product.STOCKDETAIL_FIELD_NAME;
import static adn.service.internal.Role.PERSONNEL;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import adn.application.context.internal.EffectivelyFinal;
import adn.controller.query.specification.ProductQuery;
import adn.dao.generic.Repository;
import adn.dao.generic.Result;
import adn.dao.specification.GenericFactorRepository;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.Category;
import adn.model.entities.Product;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Service
public class ProductService extends AbstractFactorService<Product> implements Service, EffectivelyFinal {

	private final ResourceService resourceService;
	private final StockDetailService stockDetailService;

	public static final int MAXIMUM_IMAGES_AMOUNT = 20;
	protected static String PRODUCT_ENTITY_NAME;

	@Autowired
	public ProductService(GenericCRUDService crudService, ResourceService resourceService, Repository repository,
			DepartmentBasedModelPropertiesFactory departmentBasedModelFactory,
			AuthenticationBasedModelFactory modelFactory,
			AuthenticationBasedModelPropertiesFactory authenticationBasedPropertiesFactory,
			StockDetailService stockDetailService, GenericFactorRepository genericFactorReopsitory) {
		super(crudService, repository, genericFactorReopsitory);
		this.resourceService = resourceService;
		this.stockDetailService = stockDetailService;
	}

	public List<Map<String, Object>> getProductsByCategory(String categoryIdentifier, String categoryIdentifierProperty,
			Collection<String> requestedColumns, Pageable paging, Role role) throws NoSuchFieldException {
		if (role == PERSONNEL) {
			return crudService.readByAssociation(Product.class, Category.class, Product.CATEGORY_FIELD_NAME,
					categoryIdentifierProperty, categoryIdentifier, requestedColumns, paging, role);
		}

		Collection<String> validatedColumns = crudService.getDefaultColumns(Product.class, role, requestedColumns);
		List<Tuple> rows = genericFactorRepository.findAllActive(Product.class, requestedColumns, paging,
				new Specification<Product>() {
					@Override
					public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						try {
							return builder.equal(root.get(Product.CATEGORY_FIELD_NAME)
									.get(!StringHelper.hasLength(categoryIdentifierProperty)
											? Category.IDENTIFIER_FIELD_NAME
											: categoryIdentifierProperty),
									categoryIdentifier);
						} catch (IllegalArgumentException e) {
							return null;
						}
					}
				});

		return crudService.resolveReadResults(Product.class, toRows(rows), from(validatedColumns), role);
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
				return bad(Map.of("images", UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			product.setImages(Stream.of(uploadResult.getBody()).collect(Collectors.toSet()));
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
		Set<String> newImagesState = model.getImages();
		Set<String> removedImages = persistence.getImages().stream()
				.filter(filename -> !newImagesState.contains(filename)).collect(Collectors.toSet());

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
				return bad(Map.of("images", UPLOAD_FAILURE));
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
			ProductQuery restQuery, Role role) throws NoSuchFieldException {
		return crudService.read(Product.class, requestedColumns, hasNameLike(restQuery).or(hasIdLike(restQuery)),
				pageable, role);
	}

	@Override
	public Map<String, Object> findWithActiveCheck(Serializable id, Class<Product> type,
			Collection<String> requestedColumns, Role principalRole) throws NoSuchFieldException {
		boolean stockDetailsRequired = false;
		Set<String> columns = new HashSet<>(requestedColumns);

		if (stockDetailsRequired = requestedColumns.contains(STOCKDETAIL_FIELD_NAME)) {
			columns.remove(STOCKDETAIL_FIELD_NAME);
		}

		Map<String, Object> product = super.findWithActiveCheck(id, type, columns, principalRole);

		if (!stockDetailsRequired || product == null) {
			return product;
		}

		List<Map<String, Object>> stockDetails = stockDetailService.readActiveOnly(id, Collections.emptyList(),
				principalRole);

		product.put(STOCKDETAIL_FIELD_NAME, stockDetails);

		return product;
	}

	private static Specification<Product> hasNameLike(ProductQuery restQuery) {
		return new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				if (restQuery.getName() == null || !StringHelper.hasLength(restQuery.getName().getLike())) {
					return null;
				}

				return builder.like(root.get("name"), restQuery.getName().getLike());
			}
		};
	}

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
