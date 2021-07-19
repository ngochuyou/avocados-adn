package adn.service.services;

import static adn.model.DatabaseInteractionResult.bad;
import static adn.model.DatabaseInteractionResult.failed;
import static adn.model.DatabaseInteractionResult.success;
import static adn.model.DatabaseInteractionResult.unauthorized;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import adn.application.context.ContextProvider;
import adn.dao.AbstractRepository;
import adn.model.DatabaseInteractionResult;
import adn.model.ModelContextProvider;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.service.AccountServiceObserver;
import adn.service.ObservableAccountService;
import adn.service.entity.builder.EntityBuilderProvider;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.internal.Service;
import adn.service.internal.ServiceResult;
import adn.service.resource.ResourceSession;

@org.springframework.stereotype.Service
public class AccountService extends DefaultCRUDService implements Service, ObservableAccountService {

	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

	public static final String UNKNOWN_USER_FIRSTNAME = "APP";
	public static final String UNKNOWN_USER_LASTNAME = "USER";
	protected static final String INVALID_ROLE = "Invalid role";
	protected static final String UPLOAD_FAILURE = "Unable to upload file";

	private final Map<String, AccountServiceObserver> observers = new HashMap<>(10);

	protected final ResourceService resourceService;

	@Autowired
	protected ResourceSession resourceSession;
	// @formatter:off
	private final Map<Role, Class<? extends Account>> roleClassMap = Map.of(
			Role.ADMIN, Admin.class,
			Role.CUSTOMER, Customer.class,
			Role.PERSONNEL, Personnel.class,
			Role.ANONYMOUS, Account.class);

	public static final String DEFAULT_ACCOUNT_PHOTO_NAME = "1619973416467_0c46022fcfda4d9f4bb8c09e8c42e9efc12d839d35c78c73e4dab1d24fac8a1c.png";
	// keep this constructor
	@Autowired
	public AccountService(
			AbstractRepository repository,
			EntityBuilderProvider entityBuilderProvider,
			AuthenticationBasedModelPropertiesFactory authenticationBasedModelPropertiesFactory,
			AuthenticationBasedModelFactory authenticationBasedModelFactory,
			ModelContextProvider modelContext,
			ResourceService resourceService) {
		super(repository, entityBuilderProvider, authenticationBasedModelPropertiesFactory, authenticationBasedModelFactory, modelContext);
		this.resourceService = resourceService;
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

	public <T extends Account, E extends T> DatabaseInteractionResult<E> create(Serializable id, E account,
			Class<E> type, MultipartFile photo, boolean flushOnFinish) {
		id = resolveId(id, account);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		boolean isResourceSessionFlushed = false;

		if (photo != null) {
			ServiceResult<String> uploadResult = resourceService.uploadImage(photo);

			if (!uploadResult.isOk()) {
				return failed(Map.of("photo", UPLOAD_FAILURE));
			}

			isResourceSessionFlushed = true;
			account.setPhoto(uploadResult.getBody());
		}

		DatabaseInteractionResult<E> insertResult = create(account.getId(), account, type, false);

		resourceService.closeSession(isResourceSessionFlushed && insertResult.isOk());

		return finish(ss, insertResult, flushOnFinish);
	}

	public <T extends Account, E extends T> DatabaseInteractionResult<E> update(Serializable id, E account,
			Class<E> type, MultipartFile photo, boolean flushOnFinish) {
		id = resolveId(id, account);

		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		E persistence = ss.load(type, id);
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!persistence.getRole().equals(account.getRole())) {
			// determine role update, currently only administrators could update account
			// role
			if (!principalRole.equals(Role.ADMIN)) {
				return unauthorized(account, Map.of(Account.ROLE_FIELD_NAME, INVALID_ROLE));
			}

			if (!persistence.getRole().canBeUpdatedTo(account.getRole())) {
				return bad(persistence, Map.of(Account.ROLE_FIELD_NAME, String
						.format("Unable to update role from %s to %s", persistence.getRole(), account.getRole())));
			}
		}
		// don't allow password update here
		account.setPassword(null);

		ServiceResult<String> localResourceResult = updateOrUploadPhoto(persistence, photo);

		if (localResourceResult.getStatus().equals(Status.FAILED)) {
			return failed(Map.of("photo", UPLOAD_FAILURE));
		}

		boolean isResourceSessionFlushed = localResourceResult.isOk();
		// set photo upload result into account so that CRUDService inject it into the
		// persistence instead of directly setting it into persistence since CRUDService
		// will inject account#getPhoto(), which is null if we set upload result
		// directly into persistence here
		account.setPhoto(localResourceResult.getBody());

		DatabaseInteractionResult<E> updateResult = update(persistence.getId(), account, type, false);

		resourceService.closeSession(isResourceSessionFlushed && updateResult.isOk());

		return finish(ss, updateResult, flushOnFinish);
	}

	public DatabaseInteractionResult<Account> deactivateAccount(String id, boolean flushOnFinish) {
		Session ss = getCurrentSession();

		ss.setHibernateFlushMode(FlushMode.MANUAL);

		Account account = ss.load(Account.class, id);

		if (!account.isActive()) {
			return DatabaseInteractionResult.error(HttpStatus.NOT_MODIFIED.value(), account,
					Map.of(Account.ACTIVE_FIELD_NAME, "Account was already deactivated"));
		}

		account.setActive(Boolean.FALSE);
		account.setDeactivatedDate(LocalDate.now());
		// use Hibernate dirty check to flush here, we don't have to call update from
		// repository to avoid unnecessary Specification validation
		return finish(ss, success(account), flushOnFinish);
	}

	private ServiceResult<String> updateOrUploadPhoto(Account persistence, MultipartFile multipartPhoto) {
		if (multipartPhoto != null) {
			if (!persistence.getPhoto().equals(DEFAULT_ACCOUNT_PHOTO_NAME)) {
				return resourceService.updateContent(multipartPhoto, persistence.getPhoto());
			}

			return resourceService.uploadImage(multipartPhoto);
		}

		return ServiceResult.<String>status(Status.UNMODIFIED).body(persistence.getPhoto());
	}

	@Override
	public void register(AccountServiceObserver observer) {
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
