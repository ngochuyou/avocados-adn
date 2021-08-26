package adn.service.services;

import static adn.dao.generic.Result.bad;
import static adn.dao.generic.Result.success;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import adn.application.context.ContextProvider;
import adn.dao.generic.Result;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.service.DomainEntityServiceObserver;
import adn.service.ObservableDomainEntityService;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;

@org.springframework.stereotype.Service
public class AccountService implements Service, ObservableDomainEntityService<Account> {

	public static final String UNKNOWN_USER_FIRSTNAME = "APP";
	public static final String UNKNOWN_USER_LASTNAME = "USER";
	protected static final String INVALID_ROLE = "Invalid role";

	private final Map<String, DomainEntityServiceObserver<Account>> observers = new HashMap<>(0);

	protected final GenericCRUDService crudService;
	protected final ResourceService resourceService;
	// @formatter:off
	private final Map<Role, Class<? extends Account>> roleClassMap = Map.of(
			Role.HEAD, Admin.class,
			Role.CUSTOMER, Customer.class,
			Role.PERSONNEL, Personnel.class,
			Role.ANONYMOUS, Account.class);

	public static final String DEFAULT_ACCOUNT_PHOTO_NAME = "1619973416467_0c46022.png";
	// keep this constructor
	@Autowired
	public AccountService(
			ResourceService resourceService,
			GenericCRUDService crudService) {
		this.resourceService = resourceService;
		this.crudService = crudService;
	}
	// @formatter:on
	@SuppressWarnings("unchecked")
	public <A extends Account> Class<A> getClassFromRole(Role role) {
		return (Class<A>) this.roleClassMap.get(role);
	}

	public <A extends Account> Role getRoleFromClass(Class<A> clazz) {
		// @formatter:off
		return this.roleClassMap
				.keySet().stream()
				.filter(key -> this.roleClassMap.get(key).equals(clazz))
				.findFirst().orElse(null);
		// @formatter:on
	}

	public <T extends Account, E extends T> Result<E> create(Serializable id, E account,
			Class<E> type, MultipartFile photo, boolean flushOnFinish) {
		id = crudService.resolveId(id, account);

		Session ss = crudService.getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		boolean isResourceSessionFlushed = false;

		if (photo != null) {
			ServiceResult<String> uploadResult = resourceService.uploadUserPhoto(photo);

			if (!uploadResult.isOk()) {
				return bad(Map.of("photo", UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			account.setPhoto(uploadResult.getBody());
		}

		Result<E> insertResult = crudService.create(account.getId(), account, type, false);

		resourceService.closeSession(isResourceSessionFlushed && insertResult.isOk() && flushOnFinish);

		return crudService.finish(ss, insertResult, flushOnFinish);
	}

	public <T extends Account, E extends T> Result<E> update(Serializable id, E account,
			Class<E> type, MultipartFile photo, boolean flushOnFinish) {
		id = crudService.resolveId(id, account);

		Session ss = crudService.getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		E persistence = ss.load(type, id);
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!persistence.getRole().equals(account.getRole())) {
			// determine role update, currently only administrators could update account
			// role
			if (!principalRole.equals(Role.HEAD)) {
				return bad(Map.of(Account.ROLE_FIELD_NAME, INVALID_ROLE));
			}

			if (!persistence.getRole().canBeUpdatedTo(account.getRole())) {
				return bad(Map.of(Account.ROLE_FIELD_NAME, String.format("Unable to update role from %s to %s",
						persistence.getRole(), account.getRole())));
			}
		}
		// don't allow password update here
		account.setPassword(null);

		ServiceResult<String> localResourceResult = updateOrUploadPhoto(persistence, photo);

		if (localResourceResult.getStatus().equals(Status.FAILED)) {
			return bad(Map.of("photo", UPLOAD_FAILURE));
		}

		boolean isResourceSessionFlushed = localResourceResult.isOk();
		// set photo upload result into account so that CRUDService inject it into the
		// persistence instead of directly setting it into persistence since CRUDService
		// will inject account#getPhoto(), which is null if we set upload result
		// directly into persistence here
		account.setPhoto(localResourceResult.getBody());

		Result<E> updateResult = crudService.update(persistence.getId(), account, type, false);

		resourceService.closeSession(isResourceSessionFlushed && updateResult.isOk());
		observers.values().forEach(observer -> observer.notifyUpdate(persistence));

		return crudService.finish(ss, updateResult, flushOnFinish);
	}

	public Result<Account> deactivateAccount(String id, boolean flushOnFinish) {
		Session ss = crudService.getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		Account account = ss.load(Account.class, id);

		if (!account.isActive()) {
			return bad(Map.of(Account.ACTIVE_FIELD_NAME, "Account was already deactivated"));
		}

		account.setActive(Boolean.FALSE);
		account.setDeactivatedDate(LocalDate.now());
		// use Hibernate dirty check to flush here, we don't have to call update from
		// repository to avoid unnecessary Specification validation
		return crudService.finish(ss, success(account), flushOnFinish);
	}

	private ServiceResult<String> updateOrUploadPhoto(Account persistence, MultipartFile multipartPhoto) {
		if (multipartPhoto != null) {
			if (!persistence.getPhoto().equals(DEFAULT_ACCOUNT_PHOTO_NAME)) {
				return resourceService.updateUserPhotoContent(multipartPhoto, persistence.getPhoto());
			}

			return resourceService.uploadUserPhoto(multipartPhoto);
		}

		return ServiceResult.<String>status(Status.UNMODIFIED).body(persistence.getPhoto());
	}

	@Override
	public void register(DomainEntityServiceObserver<Account> observer) {
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

}
