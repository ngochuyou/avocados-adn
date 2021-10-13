/**
 * 
 */
package adn.service.services;

import static adn.application.Result.bad;
import static adn.application.context.ContextProvider.getCurrentSession;
import static adn.helpers.HibernateHelper.useManualSession;
import static adn.helpers.Utils.localDateTime;
import static adn.helpers.Utils.Entry.uncheckedEntry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.builders.EntityBuilderProvider;
import adn.application.context.builders.ValidatorFactory;
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.ResultBatch;
import adn.dao.specific.ProductCostRepository;
import adn.dao.specific.ProductPriceRepository;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.helpers.Utils.Entry;
import adn.helpers.Utils.Wrapper;
import adn.model.entities.Category;
import adn.model.entities.Item;
import adn.model.entities.Product;
import adn.model.entities.ProductPrice;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._ProductPrice;
import adn.model.entities.validator.Validator;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.entity.builder.EntityBuilder;
import adn.service.internal.ResourceService;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class ProductService implements Service {

//	private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

	private final ResourceService resourceService;
	private final GenericCRUDServiceImpl crudService;
	private final ValidatorFactory validatorFactory;
	private final ProductPriceRepository productPriceRepository;
	private final ProductCostRepository productCostRepository;
	private final EntityBuilderProvider entityBuilderProvider;

	private static final String MAXIMUM_IMAGES_AMOUNT_EXCEEDED = String.format("Cannot upload more than %d files",
			_Product.MAXIMUM_IMAGES_AMOUNT);
	private static final Specification<Product> PRODUCT_IS_NOT_LOCKED = (root, query, builder) -> builder
			.isFalse(root.get(_Product.locked));
	private static final String UNMODIFIED_LOCK_STATE_TEMPLATE = "Product was already %s";
	private static final String OVERLAPPED_PRICE_TEMPLATE = "Price for product %s from %s to %s has already exsited";
	private static final String COSTS_NOT_FOUND_TEMPLATE = "Some of the following product costs were not found: %s";
	private static final String PRICES_NOT_FOUND_TEMPLATE = "Some of the following product prices were not found: %s";

	@Autowired
	public ProductService(ResourceService resourceService, GenericCRUDServiceImpl curdService,
			ProductPriceRepository productPriceRepository, EntityBuilderProvider entityBuilderProvider,
			ValidatorFactory validatorFactory, ProductCostRepository productCostRepository) {
		super();
		this.resourceService = resourceService;
		this.crudService = curdService;
		this.validatorFactory = validatorFactory;
		this.productPriceRepository = productPriceRepository;
		this.productCostRepository = productCostRepository;
		this.entityBuilderProvider = entityBuilderProvider;
	}

	public Result<Product> createProduct(Product product, MultipartFile[] images, boolean flushOnFinish) {
		Session session = getCurrentSession();

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
		Session session = getCurrentSession();

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

	public Result<Product> changeLockState(BigInteger productId, boolean requestedLockState, boolean flushOnFinish) {
		Session session = getCurrentSession();

		useManualSession(session);

		Product product = session.load(Product.class, productId);

		if (product.isLocked() == requestedLockState) {
			return Result.bad(Map.of(Common.MESSAGE, String.format(UNMODIFIED_LOCK_STATE_TEMPLATE,
					requestedLockState ? LockState.locked : LockState.unlocked)));
		}

		session.lock(product, LockMode.PESSIMISTIC_WRITE);
		product.setLocked(Boolean.valueOf(requestedLockState));
		session.save(product);

		return crudService.finish(Result.ok(product), flushOnFinish);
	}

	// @formatter:off
	public List<Map<String, Object>> readOnSaleProducts(
			Collection<String> columns,
			Pageable paging,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readOnSaleProducts(null, null, columns, paging, credential);
	}

	public List<Map<String, Object>> readOnSaleProducts(
			Long categoryIdentifier,
			String categoryIdentifierName,
			Collection<String> columns,
			Pageable paging,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		if (categoryIdentifier != null) {
			return crudService.readAllByAssociation(
					Product.class,
					Category.class,
					_Product.category,
					categoryIdentifierName,
					categoryIdentifier,
					columns,
					paging,
					credential,
					PRODUCT_IS_NOT_LOCKED);
		}

		return crudService.readAll(Product.class, columns, PRODUCT_IS_NOT_LOCKED, paging, credential);
	}
	// @formatter:on

	public Result<ProductPrice> createProductPrice(ProductPrice price, boolean flushOnFinish) {
		useManualSession();

		ProductPriceId newPriceId = price.getId();
		ProductPriceId overlappedPrice = productPriceRepository
				.findOverlapping(newPriceId.getProductId(), newPriceId.getAppliedTimestamp(),
						newPriceId.getDroppedTimestamp(), Arrays.asList(_ProductPrice.id))
				.map(cols -> (ProductPriceId) cols[0]).orElse(null);

		if (overlappedPrice != null) {
			return Result.bad(String.format(OVERLAPPED_PRICE_TEMPLATE, newPriceId.getProductId(),
					localDateTime(newPriceId.getAppliedTimestamp()), localDateTime(newPriceId.getDroppedTimestamp())));
		}

		return crudService.create(newPriceId, price, ProductPrice.class, flushOnFinish);
	}

	// we provide full path since Repository use HQL directly
	private static final List<String> FETCHED_COST_COLUMNS = Arrays.asList(
			StringHelper.join(Common.DOT, _ProductCost.id, _ProductCost.productId),
			StringHelper.join(Common.DOT, _ProductCost.id, _ProductCost.providerId), _ProductCost.cost);
	// provide only the attribute name since GenericRepository will resolve it's
	// full path
	private static final List<String> FETCHED_PRICE_COLUMNS = Arrays.asList(_ProductPrice.productId,
			_ProductPrice.price);
	private static final String MISSING_COST_TEMPLATE = "Product %d from Provider %s";
	private static final String MISSING_PRICE_TEMPLATE = "Product %d";

	public ResultBatch<Item> createItemsBatch(Collection<Item> batch, boolean flushOnFinish) {
		if (batch.size() > GenericCRUDServiceImpl.MAXIMUM_BATCH_SIZE) {
			return ResultBatch.bad(GenericCRUDServiceImpl.MAXIMUM_BATCH_SIZE_EXCEEDED);
		}

		Session session = getCurrentSession();

		useManualSession(session);

		EntityBuilder<Item> builder = entityBuilderProvider.getBuilder(Item.class);
		Validator<Item> validator = validatorFactory.getValidator(Item.class);
		EnumSet<Status> statusSet = EnumSet.noneOf(Status.class);
		Set<Utils.Entry<BigInteger, UUID>> toBeFetchedCosts = new HashSet<>(
				GenericCRUDServiceImpl.MAXIMUM_BATCH_SIZE / 2);
		Set<BigInteger> tobeFetchedPrices = new HashSet<>(GenericCRUDServiceImpl.MAXIMUM_BATCH_SIZE / 2);
		List<Result<Item>> resultBatch = batch.stream().map(item -> {
			Item newItem = builder.buildInsertion(null, item);
			Result<Item> validation = validator.isSatisfiedBy(session, newItem.getId(), newItem);

			statusSet.add(validation.getStatus());

			if (!validation.isOk()) {
				return validation;
			}

			BigInteger productId = newItem.getProduct().getId();

			if (newItem.getCost() == null) {
				toBeFetchedCosts.add(Entry.entry(productId, newItem.getProvider().getId()));
			}

			tobeFetchedPrices.add(productId);

			return validation;
		}).collect(Collectors.toList());

		if (statusSet.contains(Status.BAD)) {
			return ResultBatch.bad(resultBatch);
		}
		/* ========================Costs fetch======================== */
		List<Object[]> costs = null;
		// wrap for effectively final
		Wrapper<Map<Utils.Entry<BigInteger, UUID>, BigDecimal>> costMapWrapper = new Wrapper<>(null);

		if (!toBeFetchedCosts.isEmpty()) {
			costs = productCostRepository.findAllCurrents(toBeFetchedCosts, FETCHED_COST_COLUMNS);

			if (costs.size() != toBeFetchedCosts.size()) {
				Set<Utils.Entry<BigInteger, UUID>> fetchedCosts = costs.stream()
						.map(row -> uncheckedEntry((BigInteger) row[0], (UUID) row[1])).collect(Collectors.toSet());

				return ResultBatch.failed(String.format(COSTS_NOT_FOUND_TEMPLATE, toBeFetchedCosts.stream()
						.filter(entry -> !fetchedCosts.contains(entry))
						.map(entry -> entry.map(
								(productId, providerId) -> String.format(MISSING_COST_TEMPLATE, productId, providerId)))
						.collect(Collectors.joining(StringHelper.COMMON_JOINER))));
			}

			costMapWrapper.setValue(costs.stream()
					.map(row -> Map.entry(uncheckedEntry((BigInteger) row[0], (UUID) row[1]), (BigDecimal) row[2]))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}
		/* ========================Prices fetch======================== */
		List<Object[]> prices = productPriceRepository.findAllCurrents(tobeFetchedPrices, FETCHED_PRICE_COLUMNS);

		if (prices.size() != tobeFetchedPrices.size()) {
			Set<BigInteger> fetchedPrices = prices.stream().map(row -> (BigInteger) row[0]).collect(Collectors.toSet());

			return ResultBatch.failed(String.format(PRICES_NOT_FOUND_TEMPLATE,
					tobeFetchedPrices.stream().filter(productId -> !fetchedPrices.contains(productId))
							.map(productId -> String.format(MISSING_PRICE_TEMPLATE, productId))
							.collect(Collectors.joining(StringHelper.COMMON_JOINER))));
		}

		Map<BigInteger, BigDecimal> priceMap = prices.stream()
				.map(row -> Map.entry((BigInteger) row[0], (BigDecimal) row[1]))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		EnumSet<Status> finalStatusSet = EnumSet.noneOf(Status.class);
		// unwrap
		Map<Utils.Entry<BigInteger, UUID>, BigDecimal> costMap = costMapWrapper.getValue();

		resultBatch = resultBatch.stream().map(result -> {
			try {
				Item item = result.getInstance();
				BigInteger productId = item.getProduct().getId();

				item.setPrice(priceMap.get(productId));

				if (item.getCost() == null) {
					item.setCost(costMap.get(uncheckedEntry(productId, item.getProvider().getId())));
				}

				item = builder.buildPostValidationOnInsert(null, item);
				session.save(item);

				return Result.ok(item);
			} catch (Exception e) {
				finalStatusSet.add(Status.FAILED);
				return Result.<Item>failed(e.getMessage());
			}
		}).collect(Collectors.toList());

		return crudService.finish(session, resultBatch, finalStatusSet, flushOnFinish);
	}

	public List<Map<String, Object>> searchProduct(Collection<String> requestedColumns, Pageable pageable,
			ProductQuery restQuery, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return crudService.readAll(Product.class, requestedColumns,
				SpecificationUtils.hasNameLike(restQuery).or(hasIdLike(restQuery)), pageable, credential);
	}

	private static Specification<Product> hasIdLike(ProductQuery restQuery) {
		return (root, query, builder) -> {
			if (restQuery.getId() == null || !StringHelper.hasLength(restQuery.getId().getLike())) {
				return null;
			}

			return builder.like(root.get("id"), restQuery.getId().getLike());
		};
	}

	private enum LockState {
		locked, unlocked
	}

}
