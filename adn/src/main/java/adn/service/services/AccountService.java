package adn.service.services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.model.DatabaseInteractionResult;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.service.AccountServiceObserver;
import adn.service.ObservableAccountService;
import adn.service.internal.CRUDService;
import adn.service.internal.Role;
import adn.service.internal.Service;

@org.springframework.stereotype.Service
public class AccountService implements Service, ObservableAccountService {

	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

	public static final String UNKNOWN_USER_FIRSTNAME = "APP";
	public static final String UNKNOWN_USER_LASTNAME = "USER";

	private final CRUDService crudService;
	private final Map<String, AccountServiceObserver> observers = new HashMap<>(10);

	// @formatter:off
	private final Map<Role, Class<? extends Account>> roleClassMap = Map.of(
			Role.ADMIN, Admin.class,
			Role.CUSTOMER, Customer.class,
			Role.PERSONNEL, Personnel.class,
			Role.ANONYMOUS, Account.class);
	// @formatter:on
	public static final String DEFAULT_ACCOUNT_PHOTO_NAME = "1619973416467_0c46022fcfda4d9f4bb8c09e8c42e9efc12d839d35c78c73e4dab1d24fac8a1c.png";

	public AccountService(CRUDService crudService) {
		this.crudService = crudService;
	}

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

	public <T extends Account, E extends T> DatabaseInteractionResult<E> update(Serializable id, E entity,
			Class<E> type) {
		DatabaseInteractionResult<E> result = crudService.update(id, entity, type);

		if (result.isOk()) {
			observers.values().forEach(observer -> observer.notifyAccountUpdate(result.getInstance()));
		}

		return result;
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
