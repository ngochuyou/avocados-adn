/**
 * 
 */
package adn.service.services;

import static adn.dao.DatabaseInteractionResult.bad;
import static adn.dao.DatabaseInteractionResult.failed;
import static adn.helpers.ArrayHelper.from;
import static adn.service.internal.Role.ADMIN;
import static adn.service.internal.Role.PERSONNEL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import adn.application.context.EffectivelyFinal;
import adn.dao.DatabaseInteractionResult;
import adn.dao.Repository;
import adn.helpers.EntityUtils;
import adn.model.entities.Category;
import adn.model.entities.Factor;
import adn.model.entities.Product;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.DomainEntityServiceObserver;
import adn.service.ObservableDomainEntityService;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class ProductService extends FactorService
		implements Service, ObservableDomainEntityService<Category>, EffectivelyFinal {

	@SuppressWarnings("unused")
	private final DepartmentBasedModelPropertiesFactory departmentBasedModelFactory;
	private final ResourceService resourceService;

	private final Map<String, DomainEntityServiceObserver<Category>> observers = new HashMap<>(0);

	public static final int MAXIMUM_IMAGES_AMOUNT = 20;
	protected static String PRODUCT_ENTITY_NAME;

	@Autowired
	public ProductService(CRUDServiceImpl crudService, ResourceService resourceService, Repository repository,
			DepartmentBasedModelPropertiesFactory departmentBasedModelFactory,
			AuthenticationBasedModelFactory modelFactory, AuthenticationBasedModelPropertiesFactory propertiesFactory) {
		super(crudService, repository, modelFactory, propertiesFactory);
		this.departmentBasedModelFactory = departmentBasedModelFactory;
		this.resourceService = resourceService;
	}

	public List<Map<String, Object>> getProductsByCategory(String categoryId, Collection<String> requestedColumns,
			Pageable paging, Role role) throws NoSuchFieldException {
		if (role == ADMIN || role == PERSONNEL) {
			return crudService.read(Product.class, requestedColumns, paging, role);
		}

		return readActiveRows(Product.class, requestedColumns, "category.id=:categoryId",
				Map.of("categoryId", categoryId), paging, role);
	}

	/**
	 * This method is cursed
	 */
	public List<List<Map<String, Object>>> getProductsByCategories(Collection<String> categoryIds,
			Collection<String> requestedColumns, Pageable paging, Role role) throws NoSuchFieldException {
		String[] validatedColumns = from(
				crudService.getDefaultColumnsOrTranslate(Product.class, role, requestedColumns));
		String[] fetchedColumns = ArrayUtils.add(validatedColumns, "category_id");

		final int categoryIdIndex = validatedColumns.length;
		Map.Entry<Integer, Long> limitOffset = crudService.resolveLimitOffset(paging);
		// @formatter:off
		final String query = String.format(
				"(SELECT %s FROM %s WHERE category_id='%s' %s LIMIT %d OFFSET %d)",
				Stream.of(fetchedColumns).collect(Collectors.joining(",")),
				"products", "%s",
				role == ADMIN || role == PERSONNEL ? "" : String.format("AND %s IS TRUE", Factor.ACTIVE_FIELD_NAME),
				limitOffset.getKey(), limitOffset.getValue());
		final int size = categoryIds.size();
		// @formatter:on
		List<String> parameterValues;

		if (!(categoryIds instanceof ArrayList)) {
			parameterValues = new ArrayList<>(categoryIds);
		} else {
			parameterValues = (ArrayList<String>) categoryIds;
		}
		// @formatter:off
		Map<String, Integer> indexMap = new HashMap<>(size);
		List<List<Map<String, Object>>> results = IntStream.range(0, size).mapToObj(index -> new ArrayList<Map<String, Object>>()).collect(Collectors.toList());
		@SuppressWarnings("unchecked")
		// this line always produce Object[]
		List<Object[]> rows = (List<Object[]>) repository.nativelyFind(
				IntStream.range(0, size).mapToObj(index -> {
					String categoryId = parameterValues.get(index);
					
					indexMap.put(categoryId, index);

					return String.format(query, categoryId);
				}).collect(Collectors.joining("\nUNION\n")));
		// @formatter:on

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		rows.stream().forEach(row -> {
			results.get(indexMap.get(row[categoryIdIndex]))
					.add(propertiesFactory.produce(Product.class, row, validatedColumns, role));
		});

		return results;
	}

	public DatabaseInteractionResult<Product> createProduct(Product product, MultipartFile[] images,
			boolean flushOnFinish) {
		Session session = crudService.getCurrentSession();

		session.setHibernateFlushMode(FlushMode.MANUAL);

		boolean isResourceSessionFlushed = false;

		if (images != null && images.length != 0) {
			if (images.length > MAXIMUM_IMAGES_AMOUNT) {
				return bad(product,
						Map.of("images", String.format("Cannot upload more than %d files", MAXIMUM_IMAGES_AMOUNT)));
			}

			ServiceResult<String[]> uploadResult = resourceService.uploadProductImages(images);

			if (!uploadResult.isOk()) {
				return failed(Map.of("images", UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			product.setImages(Stream.of(uploadResult.getBody()).collect(Collectors.toSet()));
		}

		DatabaseInteractionResult<Product> result = crudService.create(null, product, Product.class, false);

		resourceService.closeSession(result.isOk() && isResourceSessionFlushed && flushOnFinish);

		return crudService.finish(session, result, flushOnFinish);
	}

	public DatabaseInteractionResult<Product> updateProduct(Product model, MultipartFile[] savedImages,
			boolean flushOnFinish) {
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
				if (removeResult.getStatus().equals(Status.BAD)) {
					return bad(model, Map.of("images", removeResult.getBody()));
				}

				return failed(Map.of("images", removeResult.getBody()));
			}
		}

		if (savedImages.length != 0) {
			if (newImagesState.size() + savedImages.length > MAXIMUM_IMAGES_AMOUNT) {
				return bad(model,
						Map.of("images", String.format("Cannot upload more than %d files", MAXIMUM_IMAGES_AMOUNT)));
			}

			ServiceResult<String[]> uploadResult = resourceService.uploadProductImages(savedImages);

			if (!uploadResult.isOk()) {
				return failed(Map.of("images", UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			newImagesState.addAll(Set.of(uploadResult.getBody()));
		}

		model.setImages(newImagesState);

		DatabaseInteractionResult<Product> result = crudService.update(model.getId(), model, Product.class, false);

		resourceService.closeSession(isResourceSessionFlushed && result.isOk() && flushOnFinish);

		return crudService.finish(session, result, flushOnFinish);
	}

	public DatabaseInteractionResult<Category> createCategory(Category category, boolean flushOnFinish) {
		DatabaseInteractionResult<Category> result = crudService.create(category.getId(), category, Category.class,
				false);

		if (result.isOk()) {
			observers.values().forEach(observer -> observer.notifyCreation(category));
		}

		return crudService.finish(crudService.getCurrentSession(), result, flushOnFinish);
	}

	@Override
	public void register(DomainEntityServiceObserver<Category> observer) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		if (observers.containsKey(observer.getId())) {
			logger.trace(String.format("Ignoring existing observer [%s], id: [%s]", observer.getClass().getName(),
					observer.getId()));
			return;
		}

		logger.trace(String.format("Registering new observer [%s], id: [%s]", observer.getClass().getName(),
				observer.getId()));
		observers.put(observer.getId(), observer);
	}

	private Access access = new Access() {
		@Override
		public void close() {
			access = null;
		}

		public void execute() throws Exception {
			PRODUCT_ENTITY_NAME = EntityUtils.getEntityName(Product.class);
		};
	};

	@Override
	public Access getAccess() throws IllegalAccessException {
		return access;
	}

}
