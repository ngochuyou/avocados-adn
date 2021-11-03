package adn.service.services;

import static adn.application.Result.bad;
import static adn.application.Result.ok;
import static adn.application.context.ContextProvider.getCurrentSession;
import static adn.application.context.ContextProvider.getPrincipalName;
import static adn.helpers.HibernateHelper.useManualSession;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.dao.generic.GenericRepository;
import adn.dao.specific.UserRepository;
import adn.model.entities.Customer;
import adn.model.entities.Head;
import adn.model.entities.Item;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.User;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._User;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.model.models.CartItem;
import adn.service.DomainEntityServiceObserver;
import adn.service.ObservableDomainEntityService;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;

@org.springframework.stereotype.Service
public class UserService implements Service, ObservableDomainEntityService<User> {

	private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

	public static final String UNKNOWN_USER_FIRSTNAME = "APP";
	public static final String UNKNOWN_USER_LASTNAME = "USER";
	protected static final String INVALID_ROLE = "Invalid role";
	private static final String NOT_ENOUGH_ITEMS_TEMPLATE = "Not enough items for %s, requested %d items, only %d left";

	private final Map<String, DomainEntityServiceObserver<User>> observers = new HashMap<>(0);

	private final GenericCRUDServiceImpl crudService;
	private final ResourceService resourceService;
	private final GenericRepository genericRepository;
	private final UserRepository userRepository;
	// @formatter:off
	private final Map<Role, Class<? extends User>> roleClassMap = Map.of(
			Role.HEAD, Head.class,
			Role.CUSTOMER, Customer.class,
			Role.PERSONNEL, Personnel.class,
			Role.ANONYMOUS, User.class);

	public static final String DEFAULT_ACCOUNT_PHOTO_NAME = "1619973416467_0c46022.png";
	// keep this constructor
	@Autowired
	public UserService(ResourceService resourceService,
			GenericCRUDServiceImpl crudService, UserRepository userRepository, GenericRepository genericRepository) {
		this.resourceService = resourceService;
		this.crudService = crudService;
		this.genericRepository = genericRepository;
		this.userRepository = userRepository;
	}
	// @formatter:on
	@SuppressWarnings("unchecked")
	public <A extends User> Class<A> getClassFromRole(Role role) {
		return (Class<A>) this.roleClassMap.get(role);
	}

	public <A extends User> Role getRoleFromClass(Class<A> clazz) {
		// @formatter:off
		return this.roleClassMap
				.keySet().stream()
				.filter(key -> this.roleClassMap.get(key).equals(clazz))
				.findFirst().orElse(null);
		// @formatter:on
	}

	public <T extends User, E extends T> Result<E> create(Serializable id, E account, Class<E> type,
			MultipartFile photo, boolean flushOnFinish) {
		id = crudService.resolveId(id, account);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		boolean isResourceSessionFlushed = false;

		if (photo != null) {
			ServiceResult<String> uploadResult = resourceService.uploadUserPhoto(photo);

			if (!uploadResult.isOk()) {
				return bad(Map.of("photo", Common.UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			account.setPhoto(uploadResult.getBody());
		}

		Result<E> insertResult = crudService.create(account.getId(), account, type, false);

		resourceService.closeSession(isResourceSessionFlushed && insertResult.isOk() && flushOnFinish);

		return crudService.finish(ss, insertResult, flushOnFinish);
	}

	public <T extends User, E extends T> Result<E> update(Serializable id, E account, Class<E> type,
			MultipartFile photo, boolean flushOnFinish) {
		id = crudService.resolveId(id, account);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		E persistence = ss.load(type, id);
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!persistence.getRole().equals(account.getRole())) {
			// determine role update, currently only administrators could update account
			// role
			if (!principalRole.equals(Role.HEAD)) {
				return bad(Map.of(_User.role, INVALID_ROLE));
			}

			if (!persistence.getRole().canBeUpdatedTo(account.getRole())) {
				return bad(Map.of(_User.role, String.format("Unable to update role from %s to %s",
						persistence.getRole(), account.getRole())));
			}
		}
		// don't allow password update here
		account.setPassword(null);

		ServiceResult<String> localResourceResult = updateOrUploadPhoto(persistence, photo);

		if (localResourceResult.getStatus().equals(Status.FAILED)) {
			return bad(Map.of("photo", Common.UPLOAD_FAILURE));
		}

		boolean isResourceSessionFlushed = localResourceResult.isOk();
		// set photo upload result into account so that CRUDService inject it into the
		// persistence instead of directly setting it into persistence
		account.setPhoto(localResourceResult.getBody());

		Result<E> updateResult = crudService.update(persistence.getId(), account, type, false);

		resourceService.closeSession(isResourceSessionFlushed && updateResult.isOk());
		observers.values().forEach(observer -> observer.notifyUpdate(persistence));

		return crudService.finish(ss, updateResult, flushOnFinish);
	}

	public Result<User> deactivateAccount(String id, boolean flushOnFinish) {
		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		User account = ss.load(User.class, id);

		if (!account.isActive()) {
			return bad(Map.of(_User.active, "Account was already deactivated"));
		}

		account.setActive(Boolean.FALSE);
		// use Hibernate dirty check to flush here, we don't have to call update from
		// repository to avoid unnecessary Specification validation
		return crudService.finish(ss, ok(account), flushOnFinish);
	}

	private ServiceResult<String> updateOrUploadPhoto(User persistence, MultipartFile multipartPhoto) {
		if (multipartPhoto != null) {
			if (!persistence.getPhoto().equals(DEFAULT_ACCOUNT_PHOTO_NAME)) {
				return resourceService.updateUserPhotoContent(multipartPhoto, persistence.getPhoto());
			}

			return resourceService.uploadUserPhoto(multipartPhoto);
		}

		return ServiceResult.<String>status(Status.UNMODIFIED).body(persistence.getPhoto());
	}

	@Override
	public void register(DomainEntityServiceObserver<User> observer) {
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

	public List<Map<String, Object>> readCustomerCart(List<String> productColumns, List<String> itemColumns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		Map<BigInteger, BigInteger> cart = userRepository
				.findCustomerCart(getPrincipalName(), (root, query, builder) -> {
					Join<Customer, Item> itemJoin = root.join(_Customer.cart);

					return Arrays.asList(itemJoin.get(_Item.id), itemJoin.join(_Item.product).get(_Product.id));
				}).stream().collect(Collectors.toMap(pair -> (BigInteger) pair[0], pair -> (BigInteger) pair[1]));

		if (cart.isEmpty()) {
			return new ArrayList<>();
		}

		List<Map<String, Object>> producedItems = crudService.readAll(Item.class, itemColumns, credential,
				(metadata) -> genericRepository.findAll(Item.class, metadata.getColumns(),
						(root, query, builder) -> builder.in(root.get(_Item.id)).value(cart.keySet())));
		Map<BigInteger, Map<String, Object>> productsMap = crudService
				.readAll(Product.class, productColumns, credential,
						(metadata) -> genericRepository.findAll(Product.class, metadata.getColumns(),
								(root, query, builder) -> builder.in(root.get(_Product.id))
										.value(new HashSet<>(cart.values()))))
				.stream().collect(HashMap<BigInteger, Map<String, Object>>::new,
						(map, model) -> map.put((BigInteger) model.get(_Product.id), model), HashMap::putAll);

		producedItems.stream().forEach(item -> {
			item.put(_Item.product, productsMap.get(cart.get(item.get(_Item.id))));
		});

		return producedItems;
	}

	private static final Specification<Item> ITEM_IS_AVAILABLE = (root, query, builder) -> builder
			.equal(root.get(_Item.status), ItemStatus.AVAILABLE);

	public Result<Void> updateCart(String customerId, List<CartItem> cartItems, boolean flushOnFinish) {
		useManualSession();

		if (cartItems.isEmpty()) {
			doUpdateCart(Collections.emptySet(), customerId);

			return Result.ok(null);
		}

		Result<Void> result = new Result<>(null);
		Set<Item> fetchedItems = cartItems.stream().flatMap(cartItem -> {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Fetching items for %s", cartItem));
			}
			// @formatter:off
			Set<Item> items = genericRepository
					.findAll(
							Item.class,
							Arrays.asList(_Item.id),
							ITEM_IS_AVAILABLE.and((root, query, builder) -> builder.and(
									builder.equal(root.get(_Item.product).get(_Product.id), cartItem.getProductId()),
									builder.equal(root.get(_Item.color), cartItem.getColor()),
									builder.equal(root.get(_Item.namedSize), cartItem.getNamedSize()))),
							Pageable.ofSize(cartItem.getQuantity()))
					.stream().map(row -> new Item((BigInteger) row[0])).collect(Collectors.toSet());
			// @formatter:on
			if (items.size() < cartItem.getQuantity()) {
				result.bad(cartItem.toString(),
						String.format(NOT_ENOUGH_ITEMS_TEMPLATE, cartItem, cartItem.getQuantity(), items.size()));
				return Stream.of();
			}

			return items.stream();
		}).collect(Collectors.toSet());

		if (!result.isOk()) {
			return result;
		}

		doUpdateCart(fetchedItems, customerId);

		return crudService.finish(result, flushOnFinish);
	}

	private void doUpdateCart(Set<Item> items, String customerId) {
		Customer customer = getCurrentSession().load(Customer.class, customerId);

		customer.setCart(items);
	}

	public Result<List<BigInteger>> addToCart(CartItem cartItem, String customerId) {
		// @formatter:off
		Set<Item> items = genericRepository
				.findAll(
						Item.class,
						Arrays.asList(_Item.id),
						ITEM_IS_AVAILABLE.and((root, query, builder) -> {
							Subquery<BigInteger> subQuery = query.subquery(BigInteger.class);
							Root<Customer> customerRoot = subQuery.from(Customer.class);
							Join<Customer, Item> cartJoin = customerRoot.join(_Customer.cart);
							
							subQuery.select(cartJoin.get(_Item.id))
								.where(builder.and(
										builder.equal(customerRoot.get(_Customer.id), customerId),
										builder.equal(cartJoin.get(_Item.product).get(_Product.id), cartItem.getProductId())));

							return builder.and(
									builder.equal(root.get(_Item.product).get(_Product.id), cartItem.getProductId()),
									Optional.ofNullable(cartItem.getColor())
										.map(color -> builder.equal(root.get(_Item.color), color))
										.orElse(builder.conjunction()),
									Optional.ofNullable(cartItem.getNamedSize())
										.map(size -> builder.equal(root.get(_Item.namedSize), size))
										.orElse(builder.conjunction()),
									// we have to use root here
									builder.not(builder.in(root.get(_Item.id)).value(subQuery)));
						}),
						Pageable.ofSize(cartItem.getQuantity()))
				.stream().map(row -> new Item((BigInteger) row[0])).collect(Collectors.toSet());
		// @formatter:on
		if (items.size() < cartItem.getQuantity()) {
			return Result.bad(String.format(NOT_ENOUGH_ITEMS_TEMPLATE, cartItem, cartItem.getQuantity(), items.size()));
		}

		Set<Item> cart = userRepository
				.findCustomerCart(customerId,
						(root, query, builder) -> Arrays.asList(root.join(_Customer.cart).get(_Item.id)))
				.stream().map(row -> new Item((BigInteger) row[0])).collect(Collectors.toSet());

		cart.addAll(items);
		doUpdateCart(cart, customerId);

		return Result.ok(items.stream().map(Item::getId).collect(Collectors.toList()));
	}

	public Result<Void> removeFromCart(List<BigInteger> itemIds, String customerId) {
		try {
			// @formatter:off
			doUpdateCart(userRepository
					.findCustomerCart(customerId, (root, query, builder) -> Arrays.asList(root.join(_Customer.cart).get(_Item.id)))
						.stream()
						.map(row -> new Item((BigInteger) row[0]))
						.filter(item -> !itemIds.contains(item.getId()))
						.collect(Collectors.toSet()),
					customerId);
			// @formatter:on
			return Result.ok(null);
		} catch (Exception e) {
			return Result.failed(e.getMessage());
		}
	}
}
